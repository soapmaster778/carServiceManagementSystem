package com.example.demo.validation;

import com.example.demo.annotation.ValidRepairDescription;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RepairDescriptionValidator
        implements ConstraintValidator<
        ValidRepairDescription, RepairOrderDto> {

    @Override
    public boolean isValid(
            RepairOrderDto dto,
            ConstraintValidatorContext context) {

        if (dto == null || dto.status() != RepairStatus.COMPLETED) {
            return true;
        }

        String description = dto.issueDescription();

        if (description != null && description.strip().length() >= 20) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("issueDescription")
                .addConstraintViolation();

        return false;
    }
}