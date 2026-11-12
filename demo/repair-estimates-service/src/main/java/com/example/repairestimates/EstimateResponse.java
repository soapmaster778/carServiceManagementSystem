package com.example.repairestimates;

import java.math.BigDecimal;

public record EstimateResponse(BigDecimal partsCost, BigDecimal laborCost, BigDecimal totalCost, EstimateStatus status) {
    public static EstimateResponse pending() {
        return new EstimateResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, EstimateStatus.PENDING);
    }
}
