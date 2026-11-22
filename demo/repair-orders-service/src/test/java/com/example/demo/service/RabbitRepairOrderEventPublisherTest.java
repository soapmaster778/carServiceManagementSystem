package com.example.demo.service;

import com.example.events.MessageBrokerNames;
import com.example.events.RepairEventType;
import com.example.events.RepairOrderEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitRepairOrderEventPublisherTest {

    @Test
    void publishesPersistentJsonMessageAfterCommit() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitRepairOrderEventPublisher publisher = new RabbitRepairOrderEventPublisher(
                rabbitTemplate, new ObjectMapper()
        );
        RepairOrderEvent event = new RepairOrderEvent(
                UUID.randomUUID(), RepairEventType.REPAIR_ORDER_CREATED, 1L,
                "Ivan", "+380501234567", "AA1234BB", Instant.parse("2026-11-19T10:00:00Z")
        );

        publisher.publishAfterCommit(event);

        ArgumentCaptor<MessagePostProcessor> processor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(
                eq(MessageBrokerNames.REPAIR_EVENTS_EXCHANGE),
                eq(MessageBrokerNames.REPAIR_EVENTS_ROUTING_KEY),
                org.mockito.ArgumentMatchers.any(String.class),
                processor.capture()
        );
        Message message = processor.getValue().postProcessMessage(new Message("{}".getBytes(), new MessageProperties()));
        assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(message.getMessageProperties().getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
        Method method = RabbitRepairOrderEventPublisher.class
                .getMethod("publishAfterCommit", RepairOrderEvent.class);
        TransactionalEventListener annotation = method.getAnnotation(TransactionalEventListener.class);
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }

}
