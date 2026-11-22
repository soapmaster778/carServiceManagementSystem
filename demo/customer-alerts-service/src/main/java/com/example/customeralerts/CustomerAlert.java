package com.example.customeralerts;

import com.example.events.RepairEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_alerts")
public class CustomerAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_event_id", unique = true, nullable = false, updatable = false)
    private UUID sourceEventId;

    @Column(name = "repair_order_id")
    private Long repairOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private RepairEventType eventType;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CustomerAlert() {
    }

    public CustomerAlert(UUID sourceEventId, Long repairOrderId, RepairEventType eventType,
                         String customerName, String phone, String message, Instant createdAt) {
        this.sourceEventId = sourceEventId;
        this.repairOrderId = repairOrderId;
        this.eventType = eventType;
        this.customerName = customerName;
        this.phone = phone;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public UUID getSourceEventId() { return sourceEventId; }
    public Long getRepairOrderId() { return repairOrderId; }
    public RepairEventType getEventType() { return eventType; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
