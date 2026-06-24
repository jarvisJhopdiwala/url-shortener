package core.services;

import core.exception.UrlShortenerException;
import core.models.UrlShortenerRequest;
import core.models.UrlShortenerResponse;
import java.util.Optional;

public interface UrlShortenerService {

  UrlShortenerResponse shortenUrl(UrlShortenerRequest request) throws UrlShortenerException;

  Optional<UrlShortenerResponse> getOriginalUrl(String shortUrl);
}
