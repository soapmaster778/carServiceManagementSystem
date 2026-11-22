package com.example.customeralerts;

import com.example.events.RepairEventType;
import com.example.events.RepairOrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAlertServiceTest {
    @Mock
    private CustomerAlertRepository repository;

    @InjectMocks
    private CustomerAlertService service;

    @Test
    void sendsManualAlert() {
        CustomerAlertResponse response = service.send(new CustomerAlertRequest(
                "Ivan Petrenko", "+380501234567", "Your repair order is accepted"
        ));

        assertThat(response.status()).isEqualTo("SENT");
        verify(repository).save(org.mockito.ArgumentMatchers.any(CustomerAlert.class));
    }

    @Test
    void processingSameEventTwiceSavesOnlyOneAlert() {
        RepairOrderEvent event = event();
        when(repository.existsBySourceEventId(event.eventId())).thenReturn(false, true);

        service.saveRepairOrderAlert(event);
        service.saveRepairOrderAlert(event);

        ArgumentCaptor<CustomerAlert> alert = ArgumentCaptor.forClass(CustomerAlert.class);
        verify(repository, times(1)).saveAndFlush(alert.capture());
        assertThat(alert.getValue().getSourceEventId()).isEqualTo(event.eventId());
        assertThat(alert.getValue().getMessage()).contains("AA1234BB");
    }

    private RepairOrderEvent event() {
        return new RepairOrderEvent(
                UUID.randomUUID(), RepairEventType.REPAIR_COMPLETED, 17L,
                "Ivan Petrenko", "+380501234567", "AA1234BB", Instant.parse("2026-11-19T10:00:00Z")
        );
    }
}
