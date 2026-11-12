package com.example.customeralerts;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerAlertServiceTest {
    private final CustomerAlertService service = new CustomerAlertService();

    @Test
    void returnsSentStatus() {
        CustomerAlertResponse response = service.send(new CustomerAlertRequest(
                "Ivan Petrenko",
                "+380501234567",
                "Your repair order is accepted"
        ));

        assertThat(response.status()).isEqualTo("SENT");
    }
}
