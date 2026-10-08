package com.example.demo.controller;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairStatus;
import com.example.demo.service.RepairOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderRestControllerTest {

    @Mock
    private RepairOrderService service;

    @InjectMocks
    private RepairOrderRestController controller;

    @Test
    void findAll_ordersExist_returnsServiceResult() {
        RepairOrderDto order = dto(1L);
        when(service.findAll()).thenReturn(List.of(order));

        List<RepairOrderDto> result = controller.findAll();

        assertThat(result).containsExactly(order);
        verify(service).findAll();
    }

    @Test
    void findById_existingId_returnsServiceResult() {
        RepairOrderDto order = dto(1L);
        when(service.findById(1L)).thenReturn(order);

        RepairOrderDto result = controller.findById(1L);

        assertThat(result).isEqualTo(order);
        verify(service).findById(1L);
    }

    @Test
    void create_validDto_delegatesToService() {
        RepairOrderDto input = dto(null);
        RepairOrderDto saved = dto(1L);
        when(service.create(input)).thenReturn(saved);

        RepairOrderDto result = controller.create(input);

        assertThat(result).isEqualTo(saved);
        verify(service).create(input);
    }

    @Test
    void update_validDto_delegatesToService() {
        RepairOrderDto input = dto(null);
        RepairOrderDto updated = dto(2L);
        when(service.update(2L, input)).thenReturn(updated);

        RepairOrderDto result = controller.update(2L, input);

        assertThat(result).isEqualTo(updated);
        verify(service).update(2L, input);
    }

    @Test
    void delete_existingId_delegatesToService() {
        controller.delete(3L);

        verify(service).delete(3L);
    }

    @Test
    void adminStatus_returnsAvailabilityMessage() {
        AdminRestController adminController = new AdminRestController();

        assertThat(adminController.status())
                .containsEntry("status", "Administration API is available");
    }

    private RepairOrderDto dto(Long id) {
        return new RepairOrderDto(id, "Test Customer", "+380501112233", "Toyota", "Corolla",
                "AA1234BB", "Diagnostics", RepairStatus.NEW,
                LocalDateTime.of(2026, 1, 1, 9, 0));
    }
}
