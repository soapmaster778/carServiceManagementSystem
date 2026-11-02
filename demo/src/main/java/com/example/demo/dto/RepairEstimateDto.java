package com.example.demo.dto;

import java.math.BigDecimal;

public record RepairEstimateDto(
        BigDecimal partsCost,
        BigDecimal laborCost,
        BigDecimal totalCost,
        EstimateStatus status
) {
    public static RepairEstimateDto pending() {
        return new RepairEstimateDto(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                EstimateStatus.PENDING
        );
    }
}
