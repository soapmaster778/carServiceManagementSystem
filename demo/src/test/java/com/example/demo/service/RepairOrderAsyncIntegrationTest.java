package com.example.demo.service;

import com.example.demo.config.AsyncConfiguration;
import com.example.demo.dto.EstimateStatus;
import com.example.demo.dto.RepairEstimateDto;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class RepairOrderAsyncIntegrationTest {

    private static final String NOTIFICATION_ERROR = "Async method sendNotification failed";
    private AnnotationConfigApplicationContext context;
    private final TestNotificationService notificationService = new TestNotificationService();
    private final TestPartsPriceService partsPriceService = new TestPartsPriceService();
    private final TestLaborCostService laborCostService = new TestLaborCostService();

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void namedExecutorRunsAsyncMethodInConfiguredThread() throws Exception {
        startContext();
        ThreadPoolTaskExecutor executor = context.getBean("repairTaskExecutor", ThreadPoolTaskExecutor.class);
        RepairOrderAsyncTasks tasks = context.getBean(RepairOrderAsyncTasks.class);

        tasks.getPartsPrice(order()).get(2, TimeUnit.SECONDS);

        assertThat(executor.getCorePoolSize()).isEqualTo(2);
        assertThat(executor.getMaxPoolSize()).isEqualTo(2);
        assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(1);
        assertThat(partsPriceService.workerThreadName).startsWith("car-service-async-");
    }

    @Test
    void independentCalculationsRunConcurrentlyAndAreCombined() throws Exception {
        startContext();
        RepairOrderEstimateService service = context.getBean(RepairOrderEstimateService.class);
        long startedAt = System.nanoTime();

        RepairEstimateDto result = service.calculateEstimate(order()).get(3, TimeUnit.SECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        assertThat(result.partsCost()).isEqualByComparingTo("500");
        assertThat(result.laborCost()).isEqualByComparingTo("700");
        assertThat(result.totalCost()).isEqualByComparingTo("1200");
        assertThat(result.status()).isEqualTo(EstimateStatus.READY);
        assertThat(elapsedMillis).isGreaterThanOrEqualTo(900).isLessThan(1_900);
        assertThat(partsPriceService.workerThreadName).isNotEqualTo(laborCostService.workerThreadName);
    }

    @Test
    void failedAsyncCalculationReturnsPendingEstimate() throws Exception {
        startContext();
        partsPriceService.fail = true;
        RepairOrderEstimateService service = context.getBean(RepairOrderEstimateService.class);

        RepairEstimateDto result = service.calculateEstimate(order()).get(3, TimeUnit.SECONDS);

        assertThat(result).isEqualTo(RepairEstimateDto.pending());
    }

    @Test
    void callerStopsWaitingWhenEstimateExceedsTimeout() {
        startContext();
        RepairOrderEstimateService service = context.getBean(RepairOrderEstimateService.class);

        RepairEstimateDto result = service.calculateEstimateWithin(order(), Duration.ofMillis(50));

        assertThat(result).isEqualTo(RepairEstimateDto.pending());
    }

    @Test
    void rejectedNotificationIsHandledWithoutServerError() {
        startContext();
        RepairOrderEstimateService service = context.getBean(RepairOrderEstimateService.class);

        boolean first = service.notifyOrderCreated(order());
        boolean second = service.notifyOrderCreated(order());
        boolean third = service.notifyOrderCreated(order());
        boolean fourth = service.notifyOrderCreated(order());

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        assertThat(third).isTrue();
        assertThat(fourth).isFalse();
    }

    @Test
    void fireAndForgetNotificationReturnsBeforeSlowTaskCompletes() throws Exception {
        startContext();
        RepairOrderEstimateService service = context.getBean(RepairOrderEstimateService.class);
        long startedAt = System.nanoTime();

        boolean accepted = service.notifyOrderCreated(order());
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        assertThat(accepted).isTrue();
        assertThat(elapsedMillis).isLessThan(500);
        assertThat(notificationService.started.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(notificationService.completed.await(100, TimeUnit.MILLISECONDS)).isFalse();
        assertThat(notificationService.completed.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void failedVoidAsyncTaskIsLoggedByUncaughtExceptionHandler(CapturedOutput output) throws Exception {
        startContext();
        notificationService.fail = true;
        RepairOrderEstimateService service = context.getBean(RepairOrderEstimateService.class);

        assertThat(service.notifyOrderCreated(order())).isTrue();
        assertThat(notificationService.started.await(1, TimeUnit.SECONDS)).isTrue();

        awaitNotificationError(output);
    }

    private void startContext() {
        context = new AnnotationConfigApplicationContext();
        context.register(AsyncConfiguration.class, RepairOrderAsyncTasks.class, RepairOrderEstimateService.class);
        context.registerBean(NotificationService.class, () -> notificationService);
        context.registerBean(PartsPriceService.class, () -> partsPriceService);
        context.registerBean(LaborCostService.class, () -> laborCostService);
        context.refresh();
    }

    private RepairOrderDto order() {
        return new RepairOrderDto(
                1L,
                "Test Customer",
                "+380501112233",
                "Toyota",
                "Corolla",
                "AA1234BB",
                "Diagnostics",
                RepairStatus.NEW,
                LocalDateTime.of(2026, 1, 1, 9, 0)
        );
    }

    private static class TestNotificationService implements NotificationService {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch completed = new CountDownLatch(1);
        private volatile boolean fail;

        @Override
        public void sendOrderAccepted(RepairOrderDto order) {
            started.countDown();
            waitForOneSecond();
            completed.countDown();
            if (fail) {
                throw new IllegalStateException("Notification provider is unavailable");
            }
        }
    }

    private static class TestPartsPriceService implements PartsPriceService {
        private volatile String workerThreadName;
        private volatile boolean fail;

        @Override
        public BigDecimal getPartsPrice(RepairOrderDto order) {
            workerThreadName = Thread.currentThread().getName();
            waitForOneSecond();
            if (fail) {
                throw new IllegalStateException("Parts supplier is unavailable");
            }
            return BigDecimal.valueOf(500);
        }
    }

    private static class TestLaborCostService implements LaborCostService {
        private volatile String workerThreadName;

        @Override
        public BigDecimal getLaborCost(RepairOrderDto order) {
            workerThreadName = Thread.currentThread().getName();
            waitForOneSecond();
            return BigDecimal.valueOf(700);
        }
    }

    private static void waitForOneSecond() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Test task was interrupted", exception);
        }
    }

    private void awaitNotificationError(CapturedOutput output) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!output.getAll().contains(NOTIFICATION_ERROR) && System.nanoTime() < deadline) {
            TimeUnit.MILLISECONDS.sleep(20);
        }
        assertThat(output.getAll()).contains(NOTIFICATION_ERROR);
    }
}
