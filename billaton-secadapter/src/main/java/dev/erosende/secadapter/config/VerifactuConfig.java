package dev.erosende.secadapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "verifactu")
public class VerifactuConfig {

    private String env = "PRE"; // PRE or PRO
    private Certificate certificate = new Certificate();
    private Sistema sistema = new Sistema();
    private Productor productor = new Productor();

    @Data
    public static class Certificate {
        private String path;
        private String password;
    }

    @Data
    public static class Sistema {
        private String id = "01";
        private String version = "1.0.0";
    }

    @Data
    public static class Productor {
        private String nif;
        private String nombre;
    }

    public String getSoapEndpoint() {
        return "PRO".equalsIgnoreCase(env)
                ? "https://www1.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP"
                : "https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP";
    }
}
