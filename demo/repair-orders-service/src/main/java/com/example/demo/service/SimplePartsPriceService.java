package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
public class SimplePartsPriceService implements PartsPriceService {

    @Override
    public BigDecimal getPartsPrice(RepairOrderDto order) {
        waitForRemoteResponse();
        return BigDecimal.valueOf(Math.max(500, order.issueDescription().length() * 45L));
    }

    private void waitForRemoteResponse() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Parts supplier request was interrupted", exception);
        }
    }
}
