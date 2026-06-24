package core.services.impl;

import core.exception.ErrorCode;
import core.exception.UrlShortenerException;
import core.models.UrlShortenerRecord;
import core.models.UrlShortenerRequest;
import core.models.UrlShortenerResponse;
import core.models.UrlShortenerResponse.UrlType;
import core.repository.UrlShortenerRepository;
import core.services.UrlShortenerService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class UrlShortenerServiceImpl implements UrlShortenerService {

  private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int SHORT_URL_LENGTH = 7;
  private static final int MAX_HASH_BYTES = 5;
  private static final int MAX_RETRIES = 5;

  private final UrlShortenerRepository repository;

  public UrlShortenerServiceImpl(UrlShortenerRepository repository) {
    this.repository = repository;
  }

  @Override
  public UrlShortenerResponse shortenUrl(@NotNull UrlShortenerRequest request) {
    var url = request.getUrl();
    url = UrlSanitizer.sanitizeUrl(url);
    StringBuilder salt = new StringBuilder();
    int attempts = 0;
    while (attempts < MAX_RETRIES) {
      String toHashUrl = url + salt;
      byte[] hashBytes = generateMd5Hash(toHashUrl);
      long hashLong = convertBytesToLong(hashBytes);
      String encodedUrl = encodeBase62(hashLong);
      encodedUrl = padToLength(encodedUrl, SHORT_URL_LENGTH);
      Optional<UrlShortenerRecord> existingRecordOpt = repository.get(encodedUrl);
      if (existingRecordOpt.isEmpty()) {
        final long currentTimeMillis = System.currentTimeMillis();
        UrlShortenerRecord record = new UrlShortenerRecord(url, encodedUrl, currentTimeMillis,
            currentTimeMillis + request.getTtlInSeconds() * 1000L);
        try {
          repository.save(record);
          return new UrlShortenerResponse(encodedUrl, UrlType.SHORT, record.getExpiresAt());
        } catch (UrlShortenerException e) {
          if(e.getErrorCode() != ErrorCode.HASH_ALREADY_EXISTS) {
            throw e;
          }
        }
      }
      else if (existingRecordOpt.get().getOriginalUrl().equals(url)) {
        return new UrlShortenerResponse(encodedUrl, UrlType.SHORT, existingRecordOpt.get().getExpiresAt());
      }
      salt.append("[RETRY]");
      attempts++;
    }
    throw UrlShortenerException.builder().errorCode(ErrorCode.HASH_NOT_GENERATED)
        .message("Failed to generate short URL after " + MAX_RETRIES + " attempts").build();
  }

  @Override
  public Optional<UrlShortenerResponse> getOriginalUrl(String shortUrl) {
    Optional<UrlShortenerRecord> recordOpt = repository.get(shortUrl);
    return recordOpt.map(
        urlShortenerRecord -> new UrlShortenerResponse(urlShortenerRecord.getOriginalUrl(),
            UrlType.ORIGINAL, urlShortenerRecord.getExpiresAt()));
  }

  private byte[] generateMd5Hash(@NotNull String input) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      return messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("hashing algorithm not found", e);
    }
  }

  private long convertBytesToLong(byte[] bytes) {
    long result = 0;
    for (int i = 0; i < UrlShortenerServiceImpl.MAX_HASH_BYTES; i++) {
      result = (result << 8) | (bytes[i] & 0xFF);
    }
    return Math.abs(result);
  }

  private String encodeBase62(long value) {
    StringBuilder sb = new StringBuilder();
    if (value == 0) {
      return String.valueOf(BASE62_ALPHABET.charAt(0));
    }
    while (value > 0) {
      int remainder = (int) (value % 62);
      sb.append(BASE62_ALPHABET.charAt(remainder));
      value /= 62;
    }
    return sb.reverse().toString();
  }

  private String padToLength(String str, int length) {
    StringBuilder strBuilder = new StringBuilder(str);
    while (strBuilder.length() < length) {
      strBuilder.insert(0, "0");
    }
    str = strBuilder.toString();
    return str.substring(0, length);
  }

  public static class UrlSanitizer {

    public static @NotNull String sanitizeUrl(@NotNull String rawUrl) {
      String url = rawUrl.trim();
      url = url.replaceAll("\\s+", ""); /* removing internal whitespaces */
      int schemaEnd = url.indexOf("://");
      if (schemaEnd != -1) {
        String schema = url.substring(0, schemaEnd).toLowerCase();
        return schema + url.substring(schemaEnd);
      }
      return url;
    }
  }
}
