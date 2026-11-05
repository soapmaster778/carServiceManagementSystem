package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzConfigurationTest {
    private final QuartzConfiguration configuration = new QuartzConfiguration();

    @Test
    void cleanupTriggerRunsImmediatelyAfterMisfire() {
        JobDetail cleanupJob = configuration.repairCleanupJobDetail();
        SimpleTrigger trigger = (SimpleTrigger) configuration.repairCleanupTrigger(cleanupJob);

        JobDataMap mergedData = new JobDataMap(cleanupJob.getJobDataMap());
        mergedData.putAll(trigger.getJobDataMap());

        assertThat(trigger.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        assertThat(mergedData.getInt("daysOld")).isEqualTo(60);
    }
}