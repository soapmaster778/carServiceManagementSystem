package com.example.demo.service;

import com.example.demo.annotation.NormalizeLicensePlate;
import com.example.demo.annotation.RetryTransientRead;
import com.example.demo.annotation.WorkshopWritable;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairOrder;
import com.example.demo.exception.DuplicateLicensePlateException;
import com.example.demo.exception.RepairOrderNotFoundException;
import com.example.demo.mapper.RepairOrderMapper;
import com.example.demo.repository.RepairOrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairOrderService {
    private final RepairOrderRepository repository;
    private final RepairOrderMapper mapper;

    @RetryTransientRead
    public List<RepairOrderDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @RetryTransientRead
    public RepairOrderDto findById(Long id) {
        return mapper.toDto(getEntity(id));
    }


    public List<RepairOrderDto> findByIds(List<Long> ids) {
        return ids.stream().map(id -> this.findById(id)).toList();
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
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @WorkshopWritable
    @NormalizeLicensePlate
    public RepairOrderDto update(Long id, RepairOrderDto dto) {
        RepairOrder entity = getEntity(id);
        if (repository.existsByLicensePlateIgnoreCaseAndIdNot(dto.licensePlate(), id)) {
            throw new DuplicateLicensePlateException(dto.licensePlate());
        }
        mapper.update(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
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
}
