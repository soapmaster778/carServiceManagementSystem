package com.example.demo.controller;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairStatus;
import com.example.demo.service.RepairOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderWebControllerTest {

    @Mock
    private RepairOrderService service;
    @Mock
    private Model model;
    @Mock
    private Authentication authentication;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RepairOrderWebController controller;

    @Test
    void home_always_redirectsToOrders() {
        assertThat(controller.home()).isEqualTo("redirect:/orders");
    }

    @Test
    void list_authenticatedUser_addsOrdersAndUsername() {
        List<RepairOrderDto> orders = List.of(dto(1L));
        when(service.findAll()).thenReturn(orders);
        when(authentication.getName()).thenReturn("manager");

        String view = controller.list(model, authentication);

        assertThat(view).isEqualTo("orders/list");
        verify(model).addAttribute("orders", orders);
        verify(model).addAttribute("username", "manager");
    }

    @Test
    void createForm_alwaysAddsBlankOrderAndStatuses() {
        String view = controller.createForm(model);

        assertThat(view).isEqualTo("orders/form");
        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("order"),
                org.mockito.ArgumentMatchers.any(RepairOrderDto.class));
        verify(model).addAttribute("statuses", RepairStatus.values());
    }

    @Test
    void create_validationErrors_returnsFormWithoutCallingService() {
        RepairOrderDto input = dto(null);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.create(input, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("orders/form");
        verify(model).addAttribute("statuses", RepairStatus.values());
        verify(service, never()).create(input);
    }

    @Test
    void create_validInput_createsOrderAndRedirects() {
        RepairOrderDto input = dto(null);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = controller.create(input, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/orders");
        verify(service).create(input);
        verify(redirectAttributes).addFlashAttribute("message", "Repair order created");
    }

    @Test
    void editForm_existingOrder_addsOrderAndStatuses() {
        RepairOrderDto order = dto(5L);
        when(service.findById(5L)).thenReturn(order);

        String view = controller.editForm(5L, model);

        assertThat(view).isEqualTo("orders/form");
        verify(model).addAttribute("order", order);
        verify(model).addAttribute("statuses", RepairStatus.values());
    }

    @Test
    void update_validationErrors_returnsFormWithoutCallingService() {
        RepairOrderDto input = dto(5L);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.update(5L, input, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("orders/form");
        verify(model).addAttribute("statuses", RepairStatus.values());
        verify(service, never()).update(5L, input);
    }


    private RepairOrderDto dto(Long id) {
        return new RepairOrderDto(id, "Test Customer", "+380501112233", "Toyota", "Corolla",
                "AA1234BB", "Diagnostics", RepairStatus.NEW,
                LocalDateTime.of(2026, 1, 1, 9, 0));
    }
}
