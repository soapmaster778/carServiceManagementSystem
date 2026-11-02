package com.example.demo.service;

import com.example.demo.dto.RepairEstimateDto;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.dto.EstimateStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class RepairOrderEstimateService {

    private static final Logger log = LoggerFactory.getLogger(RepairOrderEstimateService.class);

    private final RepairOrderAsyncTasks asyncTasks;

    public boolean notifyOrderCreated(RepairOrderDto order) {
        try {
            asyncTasks.sendNotification(order);
            return true;
        } catch (TaskRejectedException exception) {
            log.warn("Notification queue is full for repair order {}", order.licensePlate());
            return false;
        }
    }

    public CompletableFuture<RepairEstimateDto> calculateEstimate(RepairOrderDto order) {
        try {
            CompletableFuture<BigDecimal> partsCost = asyncTasks.getPartsPrice(order);
            CompletableFuture<BigDecimal> laborCost = asyncTasks.getLaborCost(order);

            return partsCost.thenCombine(laborCost,
                            (parts, labor) -> new RepairEstimateDto(
                                    parts,
                                    labor,
                                    parts.add(labor),
                                    EstimateStatus.READY
                            ))
                    .orTimeout(3, TimeUnit.SECONDS)
                    .exceptionally(exception -> {
                        log.warn("Estimate calculation failed for {}", order.licensePlate(), exception);
                        return RepairEstimateDto.pending();
                    });
        } catch (TaskRejectedException exception) {
            log.warn("Estimate queue is full for repair order {}", order.licensePlate());
            return CompletableFuture.completedFuture(RepairEstimateDto.pending());
        }
    }

    public RepairEstimateDto calculateEstimateWithin(RepairOrderDto order, Duration timeout) {
        try {
            return calculateEstimate(order).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            log.warn("Estimate calculation timed out for {}", order.licensePlate());
            return RepairEstimateDto.pending();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return RepairEstimateDto.pending();
        } catch (ExecutionException exception) {
            return RepairEstimateDto.pending();
        }
    }
}
