package com.example.demo.annotation;

import com.example.demo.validation.RepairDescriptionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RepairDescriptionValidator.class)
public @interface ValidRepairDescription {

    String message() default
            "Completed repair requires a description of at least 20 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}