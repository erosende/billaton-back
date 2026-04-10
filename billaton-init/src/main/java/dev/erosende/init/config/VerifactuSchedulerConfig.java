package dev.erosende.init.config;

import dev.erosende.billaton.application.domain.service.VerifactuSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerifactuSchedulerConfig {

    private final VerifactuSubmissionService verifactuSubmissionService;

    @Scheduled(fixedDelay = 120_000) // every 2 minutes
    public void processVerifactuQueue() {
        log.debug("Starting VeriFactu queue processing");
        verifactuSubmissionService.processQueue();
    }
}
