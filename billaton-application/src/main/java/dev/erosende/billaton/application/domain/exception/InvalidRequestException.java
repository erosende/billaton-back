package dev.erosende.billaton.application.domain.exception;

import lombok.Getter;

@Getter
public class InvalidRequestException extends Exception {

  public InvalidRequestException(String message) {
    super(message);
  }

}
