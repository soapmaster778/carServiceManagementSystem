package com.example.demo.job;

import com.example.demo.service.RepairCleanupService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class RepairCleanupJob implements Job {
    private static final Logger log =
            LoggerFactory.getLogger(RepairCleanupJob.class);

    private RepairCleanupService cleanupService;

    @Autowired
    public void setCleanupService(RepairCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();

        if (!data.containsKey("daysOld")) {
            throw new JobExecutionException("daysOld is required", false);
        }

        try {
            cleanupService.deleteOldCancelledOrders(data.getInt("daysOld"));
        } catch (RuntimeException exception) {
            log.error(
                    "Cleanup job failed. The next scheduled launch will try again.",
                    exception
            );
            throw new JobExecutionException(exception, false);
        }
    }
}