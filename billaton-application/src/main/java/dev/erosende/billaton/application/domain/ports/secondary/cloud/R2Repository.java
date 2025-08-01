package dev.erosende.billaton.application.domain.ports.secondary.cloud;

public interface R2Repository {

  String uploadDocument(String key, byte[] fileContent, String contentType);

  byte[] downloadDocument(String key);

}
