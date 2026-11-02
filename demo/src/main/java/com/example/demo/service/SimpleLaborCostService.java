package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
public class SimpleLaborCostService implements LaborCostService {

    @Override
    public BigDecimal getLaborCost(RepairOrderDto order) {
        waitForCalculation();
        return BigDecimal.valueOf(Math.max(700, order.issueDescription().length() * 60L));
    }

    private void waitForCalculation() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Labor cost calculation was interrupted", exception);
        }
    }
}
