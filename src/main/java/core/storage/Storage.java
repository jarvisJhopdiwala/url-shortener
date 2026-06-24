package core.storage;

import core.models.UrlShortenerRecord;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface Storage {

  void put(@NotNull UrlShortenerRecord record);

  Optional<UrlShortenerRecord> get(@NotNull String shortUrl);

  long deleteAllEntriesIfExpired();
}
