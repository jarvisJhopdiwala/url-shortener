package core.storage.local;

import core.models.UrlShortenerRecord;
import core.storage.Storage;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;


public class HashMapStorage implements Storage {

  private static final int NUM_LOCKS = 16;
  private final Map<String, UrlShortenerRecord> storage;
  private final ReadWriteLock[] locks;


  public HashMapStorage(Map<String, UrlShortenerRecord> storage) {
    this.storage = storage;
    locks = new ReadWriteLock[NUM_LOCKS];
    for (int i = 0; i < NUM_LOCKS; i++) {
      locks[i] = new ReentrantReadWriteLock();
    }
  }

  @Override
  public void put(@NotNull UrlShortenerRecord record) {
    final ReentrantReadWriteLock lock = (ReentrantReadWriteLock) locks[
        Math.abs(record.getShortUrl().hashCode()) % NUM_LOCKS];
    lock.writeLock().lock();
    try {
      UrlShortenerRecord existing = storage.putIfAbsent(record.getShortUrl(), record);
      if (existing != null) {
        throw new RuntimeException("short url already exists");
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Optional<UrlShortenerRecord> get(@NotNull String shortUrl) {
    final ReentrantReadWriteLock lock = (ReentrantReadWriteLock) locks[Math.abs(shortUrl.hashCode())
        % NUM_LOCKS];
    lock.readLock().lock();
    try {
      return Optional.ofNullable(storage.get(shortUrl));
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public long deleteAllEntriesIfExpired() {
    for (int i = 0; i < NUM_LOCKS; i++) {
      locks[i].writeLock().lock();
    }
    try {
      long deletedCount = 0;
      for (Entry<String, UrlShortenerRecord> entry : storage.entrySet()) {
        UrlShortenerRecord record = entry.getValue();
        if (record.getExpiresAt() <= Instant.now().toEpochMilli()) {
          storage.remove(entry.getKey());
          deletedCount++;
        }
      }
      return deletedCount;
    } finally {
      for (int i = NUM_LOCKS - 1; i >= 0; i--) {
        locks[i].writeLock().unlock();
      }
    }
  }
}
