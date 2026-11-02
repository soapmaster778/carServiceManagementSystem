package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class RepairOrderAsyncTasks {

    private final NotificationService notificationService;
    private final PartsPriceService partsPriceService;
    private final LaborCostService laborCostService;

    @Async("repairTaskExecutor")
    public void sendNotification(RepairOrderDto order) {
        notificationService.sendOrderAccepted(order);
    }

    @Async("repairTaskExecutor")
    public CompletableFuture<BigDecimal> getPartsPrice(RepairOrderDto order) {
        return CompletableFuture.completedFuture(partsPriceService.getPartsPrice(order));
    }

    @Async("repairTaskExecutor")
    public CompletableFuture<BigDecimal> getLaborCost(RepairOrderDto order) {
        return CompletableFuture.completedFuture(laborCostService.getLaborCost(order));
    }
}
