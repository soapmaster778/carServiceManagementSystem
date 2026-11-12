package com.example.demo.repository;

import com.example.demo.entity.RepairOrder;
import com.example.demo.entity.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    boolean existsByLicensePlateIgnoreCase(String licensePlate);
    boolean existsByLicensePlateIgnoreCaseAndIdNot(String licensePlate, Long id);

    List<RepairOrder> findByStatusAndCreatedAtBefore(RepairStatus status, LocalDateTime dateTime);
    long deleteByStatusAndCreatedAtBefore(RepairStatus status, LocalDateTime dateTime);
}