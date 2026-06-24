package core.models;


public class UrlShortenerRequest {

  public String url;
  public Integer ttlInSeconds;

  public UrlShortenerRequest(String url, Integer ttlInSeconds) {
    this.url = url;
    this.ttlInSeconds = ttlInSeconds;
  }

  public static UrlShortenerRequest of(String url, Integer ttlInSeconds) {
    return new UrlShortenerRequest(url, ttlInSeconds);
  }

  public String getUrl() {
    return url;
  }

  public Integer getTtlInSeconds() {
    return ttlInSeconds;
  }
}
