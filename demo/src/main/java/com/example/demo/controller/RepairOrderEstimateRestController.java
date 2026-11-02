package com.example.demo.controller;

import com.example.demo.dto.RepairEstimateDto;
import com.example.demo.service.RepairOrderEstimateService;
import com.example.demo.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/repair-orders")
@RequiredArgsConstructor
public class RepairOrderEstimateRestController {

    private final RepairOrderService repairOrderService;
    private final RepairOrderEstimateService estimateService;

    @GetMapping("/{id}/estimate")
    public CompletableFuture<RepairEstimateDto> estimate(@PathVariable Long id) {
        return estimateService.calculateEstimate(repairOrderService.findById(id));
    }

    @GetMapping("/{id}/notification")
    public ResponseEntity<Void> sendNotification(@PathVariable Long id) {
        boolean accepted = estimateService.notifyOrderCreated(repairOrderService.findById(id));
        return accepted
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
