package core.storage.local;

import core.exception.ErrorCode;
import core.exception.UrlShortenerException;
import core.models.UrlShortenerRecord;
import core.storage.Storage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;


public class HashMapStorage implements Storage {

  private static final int NUM_SEGMENTS = 16;
  private final Map<String, UrlShortenerRecord>[] segments;
  private final ReadWriteLock[] locks;


  @SuppressWarnings("unchecked")
  public HashMapStorage() {
    segments = new HashMap[NUM_SEGMENTS];
    locks = new ReadWriteLock[NUM_SEGMENTS];
    for (int i = 0; i < NUM_SEGMENTS; i++) {
      segments[i] = new HashMap<>();
      locks[i] = new ReentrantReadWriteLock();
    }
  }

  private int getSegmentIndex(@NotNull String key) {
    return key.hashCode() & (NUM_SEGMENTS - 1);
  }

  @Override
  public void put(@NotNull UrlShortenerRecord record) {
    int index = getSegmentIndex(record.getShortUrl());
    locks[index].writeLock().lock();
    try {
      if (segments[index].containsKey(record.getShortUrl())) {
        throw UrlShortenerException.builder().errorCode(ErrorCode.HASH_ALREADY_EXISTS).build();
      }
      segments[index].put(record.getShortUrl(), record);
    } finally {
      locks[index].writeLock().unlock();
    }
  }

  @Override
  public Optional<UrlShortenerRecord> get(@NotNull String shortUrl) {
    int index = getSegmentIndex(shortUrl);
    locks[index].readLock().lock();
    try {
      return Optional.ofNullable(segments[index].get(shortUrl));
    } finally {
      locks[index].readLock().unlock();
    }
  }

  @Override
  public long deleteAllEntriesIfExpired() {
    long deletedCount = 0;
    long now = System.currentTimeMillis();
    for(int i = 0; i < NUM_SEGMENTS; i++) {
      locks[i].writeLock().lock();
      try {
        var iterator = segments[i].entrySet().iterator();
        while(iterator.hasNext()) {
          final var entry = iterator.next();
          if(entry.getValue().getExpiresAt() <= now) {
            iterator.remove();
            deletedCount++;
          }
        }
      } finally {
        locks[i].writeLock().unlock();
      }
    }
    return deletedCount;
  }
}
