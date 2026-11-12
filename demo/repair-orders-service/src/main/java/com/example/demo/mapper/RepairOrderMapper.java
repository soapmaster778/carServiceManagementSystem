package com.example.demo.mapper;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairOrder;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RepairOrderMapper {
    RepairOrderDto toDto(RepairOrder entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    RepairOrder toEntity(RepairOrderDto dto);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void update(RepairOrderDto dto, @MappingTarget RepairOrder entity);
}
