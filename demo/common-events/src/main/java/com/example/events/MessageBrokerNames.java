package com.example.events;

public final class MessageBrokerNames {
    public static final String REPAIR_EVENTS_EXCHANGE = "repair.events";
    public static final String REPAIR_EVENTS_QUEUE = "customer-alerts.repair-events";
    public static final String REPAIR_EVENTS_ROUTING_KEY = "repair.order.changed";
    public static final String REPAIR_EVENTS_DLX = "repair.events.dlx";
    public static final String REPAIR_EVENTS_DLQ = "customer-alerts.repair-events.dlq";

    private MessageBrokerNames() {
    }
}
