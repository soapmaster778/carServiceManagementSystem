package com.example.demo.dto;

import com.example.demo.entity.RepairStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.example.demo.annotation.ValidRepairDescription;

import java.time.LocalDateTime;
@ValidRepairDescription
public record RepairOrderDto(
        Long id,
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Pattern(regexp = "^[+0-9() -]{7,30}$", message = "Invalid phone number") String phone,
        @NotBlank @Size(max = 60) String carBrand,
        @NotBlank @Size(max = 60) String carModel,
        @NotBlank @Size(max = 20) String licensePlate,
        @NotBlank @Size(max = 1000) String issueDescription,
        @NotNull RepairStatus status,
        LocalDateTime createdAt
) {
}
