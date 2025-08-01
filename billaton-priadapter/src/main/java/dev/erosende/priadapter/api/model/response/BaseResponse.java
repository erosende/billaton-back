package dev.erosende.priadapter.api.model.response;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;

@Data
@SuperBuilder
public class BaseResponse<T> {

  private String error;
  private T data;

  public static <T> BaseResponse<T> success(T data) {
    return BaseResponse.<T>builder()
        .data(data)
        .error(Strings.EMPTY)
        .build();
  }

  public static <T> BaseResponse<T> error(String errorMessage) {
    return BaseResponse.<T>builder()
        .error(errorMessage)
        .build();
  }

  public static <T> BaseResponse<T> error(Exception e, Logger log, LogLevel level) {
    logError(e, log, level);
    return BaseResponse.<T>builder()
        .error(e.getMessage())
        .build();
  }

  private static void logError(Exception e, Logger log, LogLevel level) {
    switch (level) {
      case ERROR -> log.error(e.getMessage(), e);
      case WARN -> log.warn(e.getMessage(), e);
    }
  }

}
