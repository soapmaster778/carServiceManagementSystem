package com.example.customeralerts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerAlertRepository extends JpaRepository<CustomerAlert, Long> {
    boolean existsBySourceEventId(UUID sourceEventId);
}
