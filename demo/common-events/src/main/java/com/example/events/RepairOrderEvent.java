package com.example.events;

import java.time.Instant;
import java.util.UUID;

public record RepairOrderEvent(
        UUID eventId,
        RepairEventType type,
        Long repairOrderId,
        String customerName,
        String phone,
        String licensePlate,
        Instant occurredAt
) {
}
