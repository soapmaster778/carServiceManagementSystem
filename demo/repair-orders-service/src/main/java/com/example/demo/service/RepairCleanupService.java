package com.example.demo.service;

import com.example.demo.entity.RepairStatus;
import com.example.demo.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairCleanupService {
    private final RepairOrderRepository repository;

    @Transactional
    public void deleteOldCancelledOrders(int daysOld) {
        if (daysOld < 1) {
            throw new IllegalArgumentException("daysOld must be positive");
        }

        repository.deleteByStatusAndCreatedAtBefore(
                RepairStatus.CANCELLED,
                LocalDateTime.now().minusDays(daysOld)
        );
    }
}