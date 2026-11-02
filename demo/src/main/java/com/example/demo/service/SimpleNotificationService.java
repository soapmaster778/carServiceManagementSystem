package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SimpleNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SimpleNotificationService.class);

    @Override
    public void sendOrderAccepted(RepairOrderDto order) {
        waitForRemoteResponse();
        log.info("Order-accepted notification sent to {}", order.phone());
    }

    private void waitForRemoteResponse() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Notification request was interrupted", exception);
        }
    }
}
