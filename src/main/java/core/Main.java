package core;

import core.exception.UrlShortenerException;
import core.models.UrlShortenerRequest;
import core.models.UrlShortenerResponse;
import core.repository.UrlShortenerRepository;
import core.scheduler.PeriodicScheduler;
import core.services.UrlShortenerService;
import core.services.impl.UrlShortenerServiceImpl;
import core.storage.Storage;
import core.storage.local.HashMapStorage;
import java.util.Optional;

public class Main {

  public static void main(String... args) throws UrlShortenerException {

    final Storage storage = new HashMapStorage();
    final UrlShortenerRepository repository = new UrlShortenerRepository(storage);
    final UrlShortenerService service = new UrlShortenerServiceImpl(repository);

    UrlShortenerRequest request = UrlShortenerRequest.of("https://www.example.com", 10);
    final UrlShortenerResponse shortenResponse = service.shortenUrl(request);
    final Optional<UrlShortenerResponse> actualResponseBeforeClean = service.getOriginalUrl(
        shortenResponse.getUrl());
    System.out.println(shortenResponse);
    System.out.println(actualResponseBeforeClean);
    PeriodicScheduler periodicScheduler = new PeriodicScheduler(repository);
    periodicScheduler.start();
    try {
      Thread.sleep(10 * 1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    final Optional<UrlShortenerResponse> actualResponseAfterClean = service.getOriginalUrl(
        shortenResponse.getUrl());
    System.out.println(actualResponseAfterClean);
    periodicScheduler.stop();
  }
}