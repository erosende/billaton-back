package dev.erosende.secadapter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class VerifactuSslConfig {

    private final VerifactuConfig verifactuConfig;

    @Bean
    public SSLSocketFactory verifactuSslSocketFactory() {
        try {
            String certPath = verifactuConfig.getCertificate().getPath();
            String certPassword = verifactuConfig.getCertificate().getPassword();

            if (certPath == null || certPath.isBlank()) {
                log.warn("VeriFactu certificate path not configured — SOAP client will not work");
                return (SSLSocketFactory) SSLSocketFactory.getDefault();
            }

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(certPath)) {
                keyStore.load(fis, certPassword.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, certPassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // default trust store (JDK cacerts)

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            log.info("VeriFactu SSL context initialized with certificate: {}", certPath);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            log.error("Failed to initialize VeriFactu SSL context: {}", e.getMessage());
            return (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
    }
}
