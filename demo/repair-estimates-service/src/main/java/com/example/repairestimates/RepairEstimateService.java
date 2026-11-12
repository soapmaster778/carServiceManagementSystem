package com.example.repairestimates;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RepairEstimateService {
    private final EstimateTasks tasks;

    public RepairEstimateService(EstimateTasks tasks) {
        this.tasks = tasks;
    }

    public CompletableFuture<EstimateResponse> calculate(EstimateRequest request) {
        return tasks.calculatePartsCost(request.issueDescription())
                .thenCombine(tasks.calculateLaborCost(request.issueDescription()),
                        (parts, labor) -> new EstimateResponse(parts, labor, parts.add(labor), EstimateStatus.READY))
                .exceptionally(error -> EstimateResponse.pending());
    }
}
