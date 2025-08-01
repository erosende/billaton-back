package dev.erosende.priadapter.api.constant;

public final class ResponseMessage {

  private ResponseMessage() {
  }

  // Generic Success Messages
  public static final String SUCCESS_OPERATION = "Operation completed successfully";
  public static final String SUCCESS_RETRIEVAL = "Data retrieved successfully";
  public static final String SUCCESS_CREATION = "Resource created successfully";
  public static final String SUCCESS_UPDATE = "Resource updated successfully";
  public static final String SUCCESS_DELETION = "Resource deleted successfully";

  // Generic Error Messages
  public static final String ERROR_NOT_FOUND = "Resource not found";
  public static final String ERROR_BAD_REQUEST = "Invalid request";
  public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
  public static final String ERROR_FORBIDDEN = "Access forbidden";
  public static final String ERROR_INTERNAL = "Internal server error";
  public static final String ERROR_VALIDATION = "Validation error";

}
