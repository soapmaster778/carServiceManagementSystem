package com.example.demo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RepairOrderTest {

    @Test
    void onCreate_missingDefaults_setsStatusAndCreationTime() {

        RepairOrder order = new RepairOrder();

        order.onCreate();

        assertThat(order.getStatus()).isEqualTo(RepairStatus.NEW);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    void onCreate_existingValues_preservesStatusAndCreationTime() {

        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 1, 10, 0);
        RepairOrder order = new RepairOrder();
        order.setStatus(RepairStatus.COMPLETED);
        order.setCreatedAt(createdAt);


        order.onCreate();


        assertThat(order.getStatus()).isEqualTo(RepairStatus.COMPLETED);
        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
    }
}
