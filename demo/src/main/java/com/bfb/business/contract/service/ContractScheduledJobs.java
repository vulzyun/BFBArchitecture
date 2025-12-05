package com.bfb.business.contract.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "bfb.scheduling.mark-late-job",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ContractScheduledJobs {

    private static final Logger logger = LoggerFactory.getLogger(ContractScheduledJobs.class);
    
    private final ContractService contractService;

    public ContractScheduledJobs(ContractService contractService) {
        this.contractService = contractService;
    }

    @Scheduled(cron = "${bfb.scheduling.mark-late-job.cron:0 0 2 * * ?}")
    public void markLateContractsJob() {
        logger.info("Starting scheduled job: Mark late contracts");
        
        try {
            int markedCount = contractService.markLateIfOverdue();
            logger.info("Scheduled job completed: {} contracts marked as LATE", markedCount);
        } catch (Exception e) {
            logger.error("Error during mark late contracts job", e);
        }
    }
}
