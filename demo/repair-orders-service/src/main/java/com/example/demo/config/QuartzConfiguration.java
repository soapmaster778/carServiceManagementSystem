package com.example.demo.config;

import com.example.demo.job.RepairCleanupJob;
import com.example.demo.job.StaleOrderCancellationJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfiguration {
    private static final String GROUP = "car-service";
    private static final TimeZone KYIV_TIME_ZONE = TimeZone.getTimeZone("Europe/Kyiv");

    @Bean
    public JobDetail staleOrderCancellationJobDetail() {
        return JobBuilder.newJob(StaleOrderCancellationJob.class)
                .withIdentity("stale-order-cancellation-job", GROUP)
                .usingJobData("daysOld", 21)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger morningCancellationTrigger(
            @Qualifier("staleOrderCancellationJobDetail") JobDetail jobDetail
    ) {
        return TriggerBuilder.newTrigger()
                .withIdentity("morning-cancellation-trigger", GROUP)
                .forJob(jobDetail.getKey())
                .usingJobData("daysOld", 14)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 9 * * ?")
                                .inTimeZone(KYIV_TIME_ZONE)
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }

    @Bean
    public Trigger weeklyCancellationTrigger(
            @Qualifier("staleOrderCancellationJobDetail") JobDetail jobDetail
    ) {
        return TriggerBuilder.newTrigger()
                .withIdentity("weekly-cancellation-trigger", GROUP)
                .forJob(jobDetail.getKey())
                .usingJobData("daysOld", 30)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 18 ? * SUN")
                                .inTimeZone(KYIV_TIME_ZONE)
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }

    @Bean
    public JobDetail repairCleanupJobDetail() {
        return JobBuilder.newJob(RepairCleanupJob.class)
                .withIdentity("repair-cleanup-job", GROUP)
                .usingJobData("daysOld", 60)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger repairCleanupTrigger(
            @Qualifier("repairCleanupJobDetail") JobDetail jobDetail
    ) {
        return TriggerBuilder.newTrigger()
                .withIdentity("repair-cleanup-trigger", GROUP)
                .forJob(jobDetail.getKey())
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInHours(24 * 7)
                                .repeatForever()
                                .withMisfireHandlingInstructionFireNow()
                )
                .build();
    }
}