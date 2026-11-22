package com.example.customeralerts;

import com.example.events.RepairEventType;
import com.example.events.RepairOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerAlertService {
    private static final Logger log = LoggerFactory.getLogger(CustomerAlertService.class);
    private final CustomerAlertRepository repository;

    public CustomerAlertService(CustomerAlertRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerAlertResponse send(CustomerAlertRequest request) {
        CustomerAlert alert = new CustomerAlert(
                UUID.randomUUID(), null, RepairEventType.REPAIR_ORDER_CREATED,
                request.customerName(), request.phone(), request.message(), Instant.now()
        );
        repository.save(alert);
        log.info("Manual alert for {} at {}: {}", request.customerName(), request.phone(), request.message());
        return new CustomerAlertResponse("SENT", request.customerName());
    }

    @Transactional
    public void saveRepairOrderAlert(RepairOrderEvent event) {
        if (repository.existsBySourceEventId(event.eventId())) {
            log.info("Repair event {} was already processed", event.eventId());
            return;
        }
        CustomerAlert alert = new CustomerAlert(
                event.eventId(), event.repairOrderId(), event.type(), event.customerName(), event.phone(),
                messageFor(event), Instant.now()
        );
        try {
            repository.saveAndFlush(alert);
            log.info("Saved {} alert for repair order {}", event.type(), event.repairOrderId());
        } catch (DataIntegrityViolationException exception) {
            log.info("Repair event {} was already processed", event.eventId());
        }
    }

    @Transactional(readOnly = true)
    public List<CustomerAlertView> findAll() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    private String messageFor(RepairOrderEvent event) {
        return switch (event.type()) {
            case REPAIR_ORDER_CREATED -> "Repair order " + event.licensePlate() + " was created";
            case REPAIR_STARTED -> "Repair for " + event.licensePlate() + " has started";
            case REPAIR_COMPLETED -> "Repair for " + event.licensePlate() + " is completed";
        };
    }

    private CustomerAlertView toView(CustomerAlert alert) {
        return new CustomerAlertView(
                alert.getSourceEventId(), alert.getRepairOrderId(), alert.getEventType(),
                alert.getCustomerName(), alert.getPhone(), alert.getMessage(), alert.getCreatedAt()
        );
    }
}
