package core.repository;

import core.exception.UrlShortenerException;
import core.models.UrlShortenerRecord;
import core.storage.Storage;
import java.util.Optional;
import java.util.logging.Logger;

public class UrlShortenerRepository {

  private final Logger log = Logger.getLogger(UrlShortenerRepository.class.getName());
  private final Storage storage;

  public UrlShortenerRepository(Storage storage) {
    this.storage = storage;
  }

  public Optional<UrlShortenerRecord> get(String shortUrl) {
    Optional<UrlShortenerRecord> recordOptional = storage.get(shortUrl);
    if (recordOptional.isEmpty()) {
      return Optional.empty();
    }

    UrlShortenerRecord record = recordOptional.get();
    if (record.getExpiresAt() != null && record.getExpiresAt() < System.currentTimeMillis()) {
      return Optional.empty();
    }
    return recordOptional;
  }

  public void save(UrlShortenerRecord record) throws UrlShortenerException {
    storage.put(record);
  }

  public long deleteExpiredUrls() {
    long deleted = storage.deleteAllEntriesIfExpired();
    log.info("Deleted " + deleted + " expired URLs from storage.");
    return deleted;
  }
}
