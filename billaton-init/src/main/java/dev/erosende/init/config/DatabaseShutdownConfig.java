package dev.erosende.init.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseShutdownConfig {

  private final HikariDataSource dataSource;

  @PreDestroy
  public void cleanup() {
    log.info("****** Cerrando conexiones de base de datos ********");
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      log.info("Conexiones de base de datos cerradas correctamente");
    }
  }

}