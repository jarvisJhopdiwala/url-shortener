package core.services;

import core.models.UrlShortenerRequest;
import core.models.UrlShortenerResponse;
import java.util.Optional;

public interface UrlShortenerService {

  UrlShortenerResponse shortenUrl(UrlShortenerRequest request);

  Optional<UrlShortenerResponse> getOriginalUrl(String shortUrl);
}
