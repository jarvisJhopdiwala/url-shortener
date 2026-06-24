package core.models;

public class UrlShortenerResponse {

  private final String url;
  private final UrlType urlType;
  private final Long expiryTime;

  public UrlShortenerResponse(String url, UrlType urlType, Long expiryTime) {
    this.url = url;
    this.urlType = urlType;
    this.expiryTime = expiryTime;
  }

  public String getUrl() {
    return url;
  }

  public UrlType getUrlType() {
    return urlType;
  }

  public Long getExpiryTime() {
    return expiryTime;
  }

  public String toString() {
    return "UrlShortenerResponse{" +
        "url='" + url + '\'' +
        ", urlType=" + urlType +
        ", expiryTime=" + expiryTime +
        '}';
  }

  public enum UrlType {
    SHORT, ORIGINAL
  }
}
