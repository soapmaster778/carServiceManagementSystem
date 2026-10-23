package com.example.demo.aspect;

import com.example.demo.annotation.RetryTransientRead;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairOrder;
import com.example.demo.entity.RepairStatus;
import com.example.demo.exception.DuplicateLicensePlateException;
import com.example.demo.exception.RepairOrderNotFoundException;
import com.example.demo.repository.RepairOrderRepository;
import com.example.demo.service.RepairOrderService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "workshop.maintenance.read-only=false")
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
@ExtendWith(OutputCaptureExtension.class)
class RepairOrderAopIntegrationTest {
    @Autowired
    private RepairOrderService service;
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RepairOrderRepository repository;
    @MockitoSpyBean
    private RetryTransientReadAspect retryAspect;

    @Test
    void selfInvocationOfSameMethodDoesNotRetry(CapturedOutput output) {
        when(repository.findById(1L)).thenThrow(transientFailure()).thenReturn(Optional.of(entity()));

        assertThatThrownBy(() -> service.findByIds(List.of(1L)))
                .isInstanceOf(TransientDataAccessResourceException.class);

        verify(repository).findById(1L);
        verifyNoInteractions(retryAspect);
        assertThat(output).doesNotContain("Retrying transient database read:");
    }

    @Test
    void successfulSelfInvocationSilentlyBypassesAdvice(CapturedOutput output) {
        when(repository.findById(1L)).thenReturn(Optional.of(entity()));

        assertThat(service.findByIds(List.of(1L))).extracting(RepairOrderDto::id).containsExactly(1L);

        verify(repository).findById(1L);
        verifyNoInteractions(retryAspect);
        assertThat(output).doesNotContain("Retrying transient database read:");
    }

    @Test
    void permanentNotFoundIsNotRetried() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(RepairOrderNotFoundException.class);

        verify(repository).findById(99L);
    }

    @Test
    void findAllAlsoRetriesThroughTheProxy() {
        when(repository.findAll()).thenThrow(transientFailure()).thenReturn(List.of(entity()));

        assertThat(service.findAll()).extracting(RepairOrderDto::id).containsExactly(1L);

        verify(repository, times(2)).findAll();
    }

    @Test
    void createThroughRestApiReturnsCanonicalPlate() throws Exception {
        when(repository.save(any(RepairOrder.class))).thenAnswer(invocation -> {
            RepairOrder order = invocation.getArgument(0);
            order.setId(1L);
            order.setCreatedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
            return order;
        });

        mvc.perform(post("/api/repair-orders").contentType("application/json").content("""
                {
                  "customerName": "Customer", "phone": "+380501112233",
                  "carBrand": "Toyota", "carModel": "Corolla",
                  "licensePlate": " aa 1234 bb ", "issueDescription": "Diagnostics",
                  "status": "NEW"
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("AA1234BB"));

        verify(repository).existsByLicensePlateIgnoreCase("AA1234BB");
        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLicensePlate()).isEqualTo("AA1234BB");
    }

    @Test
    void normalizedPlateIsUsedForDuplicateDetectionBeforeSaving() {
        when(repository.existsByLicensePlateIgnoreCase("AA1234BB")).thenReturn(true);

        assertThatThrownBy(() -> service.create(input(" aa 1234 bb ")))
                .isInstanceOf(DuplicateLicensePlateException.class);

        verify(repository).existsByLicensePlateIgnoreCase("AA1234BB");
        verify(repository, never()).save(any());
    }

    @Test
    void updateNormalizesBeforeDuplicateCheckAndPersistence() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity()));
        when(repository.save(any(RepairOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepairOrderDto result = service.update(1L, input(" bc 7788 ce "));

        assertThat(result.licensePlate()).isEqualTo("BC7788CE");
        assertThat(result.id()).isEqualTo(1L);
        verify(repository).existsByLicensePlateIgnoreCaseAndIdNot("BC7788CE", 1L);
    }

    private RepairOrderDto input(String plate) {
        return new RepairOrderDto(null, "Customer", "+380501112233", "Toyota", "Corolla",
                plate, "Diagnostics", RepairStatus.NEW, null);
    }

    private RepairOrder entity() {
        RepairOrder order = new RepairOrder();
        order.setId(1L);
        order.setLicensePlate("AA1234BB");
        order.setStatus(RepairStatus.NEW);
        return order;
    }

    private TransientDataAccessResourceException transientFailure() {
        return new TransientDataAccessResourceException("Temporary database failure");
    }
}
