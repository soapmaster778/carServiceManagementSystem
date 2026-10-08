package com.example.demo.repository;

import com.example.demo.entity.RepairOrder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    boolean existsByLicensePlateIgnoreCase(String licensePlate);
    boolean existsByLicensePlateIgnoreCaseAndIdNot(String licensePlate, Long id);
}
