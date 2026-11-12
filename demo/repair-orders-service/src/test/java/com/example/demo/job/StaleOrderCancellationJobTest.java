package com.example.demo.job;

import com.example.demo.service.StaleOrderCancellationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaleOrderCancellationJobTest {
    @Mock
    private StaleOrderCancellationService cancellationService;

    @Mock
    private JobExecutionContext context;

    @Test
    void usesDaysOldFromTriggerData() throws Exception {
        StaleOrderCancellationJob job = new StaleOrderCancellationJob();
        job.setCancellationService(cancellationService);

        JobDataMap data = new JobDataMap();
        data.put("daysOld", 14);

        when(context.getMergedJobDataMap()).thenReturn(data);

        job.execute(context);

        verify(cancellationService).cancelOldNewOrders(14);
    }

    @Test
    void throwsErrorWhenDaysOldIsMissing() {
        StaleOrderCancellationJob job = new StaleOrderCancellationJob();
        job.setCancellationService(cancellationService);

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("daysOld is required");
    }

    @Test
    void doesNotRetryForeverWhenServiceFails() {
        StaleOrderCancellationJob job = new StaleOrderCancellationJob();
        job.setCancellationService(cancellationService);

        JobDataMap data = new JobDataMap();
        data.put("daysOld", 14);

        when(context.getMergedJobDataMap()).thenReturn(data);
        doThrow(new IllegalStateException("Database error"))
                .when(cancellationService)
                .cancelOldNewOrders(14);

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class);

        verify(cancellationService, times(1)).cancelOldNewOrders(14);
    }
}