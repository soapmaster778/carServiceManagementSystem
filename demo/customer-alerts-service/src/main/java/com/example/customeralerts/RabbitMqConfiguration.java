package com.example.customeralerts;

import com.example.events.MessageBrokerNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
    @Bean
    DirectExchange repairEventsExchange() {
        return new DirectExchange(MessageBrokerNames.REPAIR_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange repairEventsDeadLetterExchange() {
        return new DirectExchange(MessageBrokerNames.REPAIR_EVENTS_DLX, true, false);
    }

    @Bean
    Queue repairEventsQueue() {
        return QueueBuilder.durable(MessageBrokerNames.REPAIR_EVENTS_QUEUE)
                .deadLetterExchange(MessageBrokerNames.REPAIR_EVENTS_DLX)
                .build();
    }

    @Bean
    Queue repairEventsDeadLetterQueue() {
        return QueueBuilder.durable(MessageBrokerNames.REPAIR_EVENTS_DLQ).build();
    }

    @Bean
    Binding repairEventsBinding(Queue repairEventsQueue, DirectExchange repairEventsExchange) {
        return BindingBuilder.bind(repairEventsQueue)
                .to(repairEventsExchange)
                .with(MessageBrokerNames.REPAIR_EVENTS_ROUTING_KEY);
    }

    @Bean
    Binding repairEventsDeadLetterBinding(Queue repairEventsDeadLetterQueue,
                                          DirectExchange repairEventsDeadLetterExchange) {
        return BindingBuilder.bind(repairEventsDeadLetterQueue)
                .to(repairEventsDeadLetterExchange)
                .with(MessageBrokerNames.REPAIR_EVENTS_ROUTING_KEY);
    }
}
