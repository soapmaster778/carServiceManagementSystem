package com.example.repairestimates;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/repair-estimates")
public class RepairEstimateController {
    private final RepairEstimateService service;

    public RepairEstimateController(RepairEstimateService service) {
        this.service = service;
    }

    @PostMapping
    public CompletableFuture<EstimateResponse> calculate(@Valid @RequestBody EstimateRequest request) {
        return service.calculate(request);
    }
}
