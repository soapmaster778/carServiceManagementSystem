package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairOrder;
import com.example.demo.entity.RepairStatus;
import com.example.demo.exception.DuplicateLicensePlateException;
import com.example.demo.exception.RepairOrderNotFoundException;
import com.example.demo.mapper.RepairOrderMapper;
import com.example.demo.repository.RepairOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private RepairOrderMapper mapper;

    @InjectMocks
    private RepairOrderService service;

    @Test
    void findAll_ordersExist_returnsMappedDtos() {

        RepairOrder first = entity(1L, "AA1234BB");
        RepairOrder second = entity(2L, "BC7788CE");
        RepairOrderDto firstDto = dto(1L, "AA1234BB");
        RepairOrderDto secondDto = dto(2L, "BC7788CE");
        when(repository.findAll()).thenReturn(List.of(first, second));
        when(mapper.toDto(first)).thenReturn(firstDto);
        when(mapper.toDto(second)).thenReturn(secondDto);


        List<RepairOrderDto> result = service.findAll();

        assertThat(result).containsExactly(firstDto, secondDto);
        verify(repository).findAll();
        verify(mapper).toDto(first);
        verify(mapper).toDto(second);
    }

    @Test
    void findAll_noOrders_returnsEmptyList() {

        when(repository.findAll()).thenReturn(List.of());

        List<RepairOrderDto> result = service.findAll();

        assertThat(result).isEmpty();
        verify(repository).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    void findById_existingId_returnsMappedDto() {

        RepairOrder entity = entity(1L, "AA1234BB");
        RepairOrderDto expected = dto(1L, "AA1234BB");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(expected);

        RepairOrderDto result = service.findById(1L);

        assertThat(result).isEqualTo(expected);
        verify(repository).findById(1L);
        verify(mapper).toDto(entity);
    }

    @Test
    void findById_missingId_throwsNotFoundException() {

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(RepairOrderNotFoundException.class)
                .hasMessage("Repair order with id 99 was not found");
        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    void create_uniqueLicensePlate_savesSanitizedEntityAndReturnsDto() {

        RepairOrderDto input = dto(null, "KA4455IT");
        RepairOrder mapped = entity(77L, "KA4455IT");
        mapped.setCreatedAt(LocalDateTime.of(2026, 10, 1, 1, 0));
        RepairOrder persisted = entity(10L, "KA4455IT");
        RepairOrderDto expected = dto(10L, "KA4455IT");
        when(repository.existsByLicensePlateIgnoreCase("KA4455IT")).thenReturn(false);
        when(mapper.toEntity(input)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(persisted);
        when(mapper.toDto(persisted)).thenReturn(expected);

        RepairOrderDto result = service.create(input);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getCreatedAt()).isNull();
        assertThat(result).isEqualTo(expected);
        verify(repository).existsByLicensePlateIgnoreCase("KA4455IT");
        verify(mapper).toEntity(input);
    }

    @Test
    void create_duplicateLicensePlate_throwsAndDoesNotSave() {

        RepairOrderDto input = dto(null, "AA1234BB");
        when(repository.existsByLicensePlateIgnoreCase("AA1234BB")).thenReturn(true);

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(DuplicateLicensePlateException.class)
                .hasMessageContaining("AA1234BB");
        verify(repository).existsByLicensePlateIgnoreCase("AA1234BB");
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void update_existingOrderAndUniquePlate_updatesAndReturnsDto() {

        RepairOrderDto input = dto(999L, "KA4455IT");
        RepairOrder existing = entity(5L, "AA1234BB");
        RepairOrderDto expected = dto(5L, "KA4455IT");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByLicensePlateIgnoreCaseAndIdNot("KA4455IT", 5L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(expected);

        RepairOrderDto result = service.update(5L, input);

        assertThat(result).isEqualTo(expected);
        assertThat(existing.getId()).isEqualTo(5L);
        verify(mapper).update(input, existing);
        verify(repository).save(existing);
        verify(repository).existsByLicensePlateIgnoreCaseAndIdNot("KA4455IT", 5L);
    }

    @Test
    void update_missingId_throwsAndDoesNotSave() {

        RepairOrderDto input = dto(null, "KA4455IT");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, input))
                .isInstanceOf(RepairOrderNotFoundException.class);
        verify(repository).findById(99L);
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void update_duplicateLicensePlate_throwsAndDoesNotSave() {

        RepairOrderDto input = dto(null, "BC7788CE");
        RepairOrder existing = entity(5L, "AA1234BB");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByLicensePlateIgnoreCaseAndIdNot("BC7788CE", 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(5L, input))
                .isInstanceOf(DuplicateLicensePlateException.class)
                .hasMessageContaining("BC7788CE");
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void delete_existingId_deletesResolvedEntity() {

        RepairOrder existing = entity(3L, "AA1234BB");
        when(repository.findById(3L)).thenReturn(Optional.of(existing));

        service.delete(3L);

        verify(repository).findById(3L);
        verify(repository).delete(existing);
    }

    @Test
    void delete_missingId_throwsAndDoesNotDelete() {

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(RepairOrderNotFoundException.class);
        verify(repository).findById(99L);
        verify(repository, never()).delete(any());
    }

    private RepairOrder entity(Long id, String licensePlate) {
        RepairOrder entity = new RepairOrder();
        entity.setId(id);
        entity.setCustomerName("Test Customer");
        entity.setPhone("+380501112233");
        entity.setCarBrand("Toyota");
        entity.setCarModel("Corolla");
        entity.setLicensePlate(licensePlate);
        entity.setIssueDescription("Diagnostics");
        entity.setStatus(RepairStatus.NEW);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        return entity;
    }

    private RepairOrderDto dto(Long id, String licensePlate) {
        return new RepairOrderDto(
                id,
                "Test Customer",
                "+380501112233",
                "Toyota",
                "Corolla",
                licensePlate,
                "Diagnostics",
                RepairStatus.NEW,
                LocalDateTime.of(2026, 1, 1, 9, 0)
        );
    }
}
