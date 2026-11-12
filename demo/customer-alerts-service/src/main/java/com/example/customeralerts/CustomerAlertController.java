package com.example.customeralerts;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-alerts")
public class CustomerAlertController {
    private final CustomerAlertService service;

    public CustomerAlertController(CustomerAlertService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CustomerAlertResponse send(@Valid @RequestBody CustomerAlertRequest request) {
        return service.send(request);
    }
}
