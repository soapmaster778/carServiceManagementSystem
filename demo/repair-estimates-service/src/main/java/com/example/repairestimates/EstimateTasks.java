package com.example.repairestimates;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
public class EstimateTasks {
    @Async("estimateTaskExecutor")
    public CompletableFuture<BigDecimal> calculatePartsCost(String description) {
        return CompletableFuture.completedFuture(BigDecimal.valueOf(Math.max(500, description.length() * 45L)));
    }

    @Async("estimateTaskExecutor")
    public CompletableFuture<BigDecimal> calculateLaborCost(String description) {
        return CompletableFuture.completedFuture(BigDecimal.valueOf(Math.max(700, description.length() * 60L)));
    }
}
