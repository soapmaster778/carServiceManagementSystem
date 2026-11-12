package com.example.customeralerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerAlertService {
    private static final Logger log = LoggerFactory.getLogger(CustomerAlertService.class);

    public CustomerAlertResponse send(CustomerAlertRequest request) {
        log.info("Alert for {} at {}: {}", request.customerName(), request.phone(), request.message());
        return new CustomerAlertResponse("SENT", request.customerName());
    }
}
