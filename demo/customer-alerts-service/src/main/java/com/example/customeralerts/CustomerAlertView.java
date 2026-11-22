package com.example.customeralerts;

import com.example.events.RepairEventType;

import java.time.Instant;
import java.util.UUID;

public record CustomerAlertView(
        UUID sourceEventId,
        Long repairOrderId,
        RepairEventType eventType,
        String customerName,
        String phone,
        String message,
        Instant createdAt
) {
}
