package core.storage;

import core.exception.UrlShortenerException;
import core.models.UrlShortenerRecord;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface Storage {

  void put(@NotNull UrlShortenerRecord record) throws UrlShortenerException;

  Optional<UrlShortenerRecord> get(@NotNull String shortUrl);

  long deleteAllEntriesIfExpired();
}
