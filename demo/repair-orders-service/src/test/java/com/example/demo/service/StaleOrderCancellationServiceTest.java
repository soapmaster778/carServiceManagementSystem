package com.example.demo.service;

import com.example.demo.entity.RepairOrder;
import com.example.demo.entity.RepairStatus;
import com.example.demo.repository.RepairOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StaleOrderCancellationServiceTest {
    @Autowired
    private RepairOrderRepository repository;

    @Test
    void secondRunDoesNotChangeOrderAgain() {
        RepairOrder order = new RepairOrder();
        order.setCustomerName("Ivan Petrenko");
        order.setPhone("+380501234567");
        order.setCarBrand("Toyota");
        order.setCarModel("Corolla");
        order.setLicensePlate("CE0001KS");
        order.setIssueDescription("Engine check");
        order.setStatus(RepairStatus.NEW);
        order.setCreatedAt(LocalDateTime.now().minusDays(20));

        RepairOrder savedOrder = repository.saveAndFlush(order);

        StaleOrderCancellationService service =
                new StaleOrderCancellationService(repository);

        service.cancelOldNewOrders(14);
        service.cancelOldNewOrders(14);

        RepairOrder result = repository.findById(savedOrder.getId()).orElseThrow();

        assertThat(result.getStatus()).isEqualTo(RepairStatus.CANCELLED);
    }
}