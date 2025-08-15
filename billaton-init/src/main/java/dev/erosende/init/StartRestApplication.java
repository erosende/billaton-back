package dev.erosende.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan({
        "dev.erosende.init",
        "dev.erosende.priadapter",
        "dev.erosende.secadapter",
        "dev.erosende.billaton.application"})
public class StartRestApplication {

    private static final Logger logger = LoggerFactory.getLogger(StartRestApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("****** Arrancando Servidor ********");
            SpringApplication.run(StartRestApplication.class, args);
        } catch (Exception e) {
            logger.error("ERROR : {}.", e.getMessage());
            e.printStackTrace();
        }
    }

}