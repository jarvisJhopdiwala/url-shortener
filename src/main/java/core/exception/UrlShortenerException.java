package core.exception;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class UrlShortenerException extends Exception {

  public final ErrorCode errorCode;

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public UrlShortenerException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public UrlShortenerException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull UrlShortenerExceptionBuilder builder() {
    return new UrlShortenerExceptionBuilder();
  }

  public static class UrlShortenerExceptionBuilder {

    private ErrorCode errorCode;
    private String message;
    private Throwable cause;

    UrlShortenerExceptionBuilder() {
    }

    public UrlShortenerExceptionBuilder errorCode(ErrorCode errorCode) {
      this.errorCode = errorCode;
      return this;
    }
    public UrlShortenerExceptionBuilder message(String message) {
      this.message = message;
      return this;
    }
    public UrlShortenerExceptionBuilder cause(Throwable cause) {
      this.cause = cause;
      return this;
    }
    public UrlShortenerException build() {
      return new UrlShortenerException(errorCode, message, cause);
    }
  }
}
