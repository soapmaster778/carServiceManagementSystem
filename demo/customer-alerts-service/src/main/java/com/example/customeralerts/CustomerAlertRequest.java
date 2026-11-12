package com.example.customeralerts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerAlertRequest(
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Pattern(regexp = "^[+0-9() -]{7,30}$") String phone,
        @NotBlank @Size(max = 500) String message
) {
}
