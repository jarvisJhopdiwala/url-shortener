package core.models;


public class UrlShortenerRecord {

  private final String originalUrl;
  private final String shortUrl;
  private final Long createdAt;
  private final Long expiresAt;

  public UrlShortenerRecord(String originalUrl, String shortUrl, Long createdAt, Long expiresAt) {
    this.originalUrl = originalUrl;
    this.shortUrl = shortUrl;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public String getShortUrl() {
    return shortUrl;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Long getExpiresAt() {
    return expiresAt;
  }

}
