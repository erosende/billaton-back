package dev.erosende.billaton.application.domain.ports.secondary.cloud;

public interface SupabaseStorageRepository {

  void uploadBackup(String key, byte[] fileContent, String contentType);

}
