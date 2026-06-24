package core.scheduler;

import core.repository.UrlShortenerRepository;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PeriodicScheduler {

  private static final Logger log = Logger.getLogger(PeriodicScheduler.class.getName());
  private static final long CLEANUP_INTERVAL_MINUTES = 30;
  private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final AtomicBoolean cleanupInProgress = new AtomicBoolean(false);
  private final UrlShortenerRepository repository;
  private ScheduledFuture<?> scheduledTask;

  public PeriodicScheduler(UrlShortenerRepository repository) {
    this.repository = repository;
  }

  public void start() {
    scheduledTask = scheduler.scheduleAtFixedRate(this::runCleanupTask, CLEANUP_INTERVAL_MINUTES,
        CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
  }

  public void stop() {
    log.info("Stopping PeriodicScheduler...");
    if (scheduledTask != null && !scheduledTask.isCancelled()) {
      scheduledTask.cancel(false);
    }
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        log.warning("Scheduler did not terminate in time, forcing shutdown...");
        scheduler.shutdownNow();
      } else {
        log.info("Scheduler terminated gracefully.");
      }
    } catch (InterruptedException e) {
      log.log(Level.SEVERE, "Interrupted while waiting for scheduler shutdown", e);
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private void runCleanupTask() {
    if (!cleanupInProgress.compareAndSet(false, true)) {
      log.info("cleanup task is already in progress, skipping this run");
      return;
    }
    try {
      log.info("Cleanup task started");
      long deleted = repository.deleteExpiredUrls();
      log.info("Cleanup task completed, deleted " + deleted + " expired URLs");
    } catch (Exception e) {
      log.log(Level.SEVERE, "error occurred during cleanup task", e);
    } finally {
      cleanupInProgress.set(false);
    }
  }
}