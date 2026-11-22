package com.example.customeralerts;

import com.example.events.MessageBrokerNames;
import com.example.events.RepairOrderEvent;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RepairOrderEventConsumer {
    private final CustomerAlertService alertService;
    private final ObjectMapper objectMapper;

    public RepairOrderEventConsumer(CustomerAlertService alertService, ObjectMapper objectMapper) {
        this.alertService = alertService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = MessageBrokerNames.REPAIR_EVENTS_QUEUE)
    public void consume(String payload) {
        try {
            RepairOrderEvent event = objectMapper.readValue(payload, RepairOrderEvent.class);
            alertService.saveRepairOrderAlert(event);
        } catch (JacksonException exception) {
            throw new AmqpRejectAndDontRequeueException("Invalid repair order event", exception);
        }
    }
}
