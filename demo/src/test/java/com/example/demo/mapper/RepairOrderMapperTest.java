package com.example.demo.mapper;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairOrder;
import com.example.demo.entity.RepairStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RepairOrderMapperTest {

    private final RepairOrderMapper mapper = Mappers.getMapper(RepairOrderMapper.class);

    @Test
    void toDto_entityProvided_mapsEveryField() {

        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 24, 12, 30);
        RepairOrder entity = entity(7L, "AA1234BB", createdAt);

        RepairOrderDto result = mapper.toDto(entity);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.customerName()).isEqualTo("Test Customer");
        assertThat(result.phone()).isEqualTo("+380501112233");
        assertThat(result.carBrand()).isEqualTo("Toyota");
        assertThat(result.carModel()).isEqualTo("Corolla");
        assertThat(result.licensePlate()).isEqualTo("AA1234BB");
        assertThat(result.issueDescription()).isEqualTo("Diagnostics");
        assertThat(result.status()).isEqualTo(RepairStatus.IN_PROGRESS);
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void toEntity_dtoProvided_mapsFieldsButIgnoresGeneratedValues() {

        RepairOrderDto dto = dto(55L, "KA4455IT");

        RepairOrder result = mapper.toEntity(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getCustomerName()).isEqualTo(dto.customerName());
        assertThat(result.getPhone()).isEqualTo(dto.phone());
        assertThat(result.getCarBrand()).isEqualTo(dto.carBrand());
        assertThat(result.getCarModel()).isEqualTo(dto.carModel());
        assertThat(result.getLicensePlate()).isEqualTo(dto.licensePlate());
        assertThat(result.getIssueDescription()).isEqualTo(dto.issueDescription());
        assertThat(result.getStatus()).isEqualTo(dto.status());
    }

    @Test
    void update_existingEntity_updatesEditableFieldsAndPreservesGeneratedValues() {

        LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 1, 1, 9, 0);
        RepairOrder entity = entity(8L, "AA1234BB", originalCreatedAt);
        RepairOrderDto dto = dto(999L, "BC7788CE");

        mapper.update(dto, entity);

        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getLicensePlate()).isEqualTo("BC7788CE");
        assertThat(entity.getCustomerName()).isEqualTo(dto.customerName());
        assertThat(entity.getStatus()).isEqualTo(dto.status());
    }

    private RepairOrder entity(Long id, String licensePlate, LocalDateTime createdAt) {
        RepairOrder entity = new RepairOrder();
        entity.setId(id);
        entity.setCustomerName("Test Customer");
        entity.setPhone("+380501112233");
        entity.setCarBrand("Toyota");
        entity.setCarModel("Corolla");
        entity.setLicensePlate(licensePlate);
        entity.setIssueDescription("Diagnostics");
        entity.setStatus(RepairStatus.IN_PROGRESS);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    private RepairOrderDto dto(Long id, String licensePlate) {
        return new RepairOrderDto(
                id,
                "Updated Customer",
                "+380671234567",
                "Skoda",
                "Octavia",
                licensePlate,
                "Brake replacement",
                RepairStatus.COMPLETED,
                LocalDateTime.of(2026, 9, 24, 14, 0)
        );
    }
}
