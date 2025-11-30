package com.bfb.business.contract.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled jobs for contract management.
 * Handles automatic maintenance tasks like marking late contracts.
 * 
 * Can be disabled by setting: bfb.scheduling.mark-late-job.enabled=false
 * Cron expression can be customized via: bfb.scheduling.mark-late-job.cron
 */
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

    /**
     * Automatically marks contracts as LATE when they exceed their end date.
     * Default schedule: Every day at 2:00 AM (configurable via application.yml)
     * 
     * This job processes all IN_PROGRESS contracts and checks if the end date has passed.
     * Transition performed: IN_PROGRESS → LATE (if endDate < today)
     */
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
