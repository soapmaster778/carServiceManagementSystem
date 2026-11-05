package com.example.demo.job;

import com.example.demo.service.StaleOrderCancellationService;
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
public class StaleOrderCancellationJob implements Job {
    private static final Logger log =
            LoggerFactory.getLogger(StaleOrderCancellationJob.class);

    private StaleOrderCancellationService cancellationService;

    @Autowired
    public void setCancellationService(
            StaleOrderCancellationService cancellationService
    ) {
        this.cancellationService = cancellationService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();

        if (!data.containsKey("daysOld")) {
            throw new JobExecutionException("daysOld is required", false);
        }

        try {
            cancellationService.cancelOldNewOrders(data.getInt("daysOld"));
        } catch (RuntimeException exception) {
            log.error(
                    "Cancellation job failed. The next scheduled launch will try again.",
                    exception
            );
            throw new JobExecutionException(exception, false);
        }
    }
}