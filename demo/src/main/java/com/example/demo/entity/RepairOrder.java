package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_orders")
@Getter
@Setter
public class RepairOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 60)
    private String carBrand;

    @Column(nullable = false, length = 60)
    private String carModel;

    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(nullable = false, length = 1000)
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RepairStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (status == null) status = RepairStatus.NEW;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
