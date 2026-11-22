package com.example.demo.service;

import com.example.events.MessageBrokerNames;
import com.example.events.RepairOrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.rabbit.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitRepairOrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(RepairOrderEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                    MessageBrokerNames.REPAIR_EVENTS_EXCHANGE,
                    MessageBrokerNames.REPAIR_EVENTS_ROUTING_KEY,
                    payload,
                    message -> {
                        message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException("Cannot serialize repair order event", exception);
        }
    }
}
