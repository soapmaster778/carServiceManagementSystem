package com.example.demo.service;

import com.example.demo.annotation.NormalizeLicensePlate;
import com.example.demo.annotation.RetryTransientRead;
import com.example.demo.annotation.WorkshopWritable;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairOrder;
import com.example.demo.entity.RepairStatus;
import com.example.demo.exception.DuplicateLicensePlateException;
import com.example.demo.exception.RepairOrderNotFoundException;
import com.example.demo.mapper.RepairOrderMapper;
import com.example.demo.repository.RepairOrderRepository;
import com.example.events.RepairEventType;
import com.example.events.RepairOrderEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairOrderService {
    private final RepairOrderRepository repository;
    private final RepairOrderMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @RetryTransientRead
    public List<RepairOrderDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @RetryTransientRead
    public RepairOrderDto findById(Long id) {
        return mapper.toDto(getEntity(id));
    }


    public List<RepairOrderDto> findByIds(List<Long> ids) {
        return ids.stream().map(this::findById).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @WorkshopWritable
    @NormalizeLicensePlate
    public RepairOrderDto create(RepairOrderDto dto) {
        if (repository.existsByLicensePlateIgnoreCase(dto.licensePlate())) {
            throw new DuplicateLicensePlateException(dto.licensePlate());
        }
        RepairOrder entity = mapper.toEntity(dto);
        entity.setId(null);
        entity.setCreatedAt(null);
        RepairOrderDto createdOrder = mapper.toDto(repository.save(entity));
        publishEvent(createdOrder, RepairEventType.REPAIR_ORDER_CREATED);
        return createdOrder;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @WorkshopWritable
    @NormalizeLicensePlate
    public RepairOrderDto update(Long id, RepairOrderDto dto) {
        RepairOrder entity = getEntity(id);
        RepairStatus previousStatus = entity.getStatus();
        if (repository.existsByLicensePlateIgnoreCaseAndIdNot(dto.licensePlate(), id)) {
            throw new DuplicateLicensePlateException(dto.licensePlate());
        }
        mapper.update(dto, entity);
        entity.setId(id);
        RepairOrderDto updatedOrder = mapper.toDto(repository.save(entity));
        eventTypeFor(previousStatus, updatedOrder.status())
                .ifPresent(type -> publishEvent(updatedOrder, type));
        return updatedOrder;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @WorkshopWritable
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    private RepairOrder getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new RepairOrderNotFoundException(id));
    }

    private void publishEvent(RepairOrderDto order, RepairEventType type) {
        eventPublisher.publishEvent(new RepairOrderEvent(
                UUID.randomUUID(),
                type,
                order.id(),
                order.customerName(),
                order.phone(),
                order.licensePlate(),
                Instant.now()
        ));
    }

    private java.util.Optional<RepairEventType> eventTypeFor(RepairStatus previous, RepairStatus current) {
        if (previous == current) {
            return java.util.Optional.empty();
        }
        return switch (current) {
            case IN_PROGRESS -> java.util.Optional.of(RepairEventType.REPAIR_STARTED);
            case COMPLETED -> java.util.Optional.of(RepairEventType.REPAIR_COMPLETED);
            default -> java.util.Optional.empty();
        };
    }
}
