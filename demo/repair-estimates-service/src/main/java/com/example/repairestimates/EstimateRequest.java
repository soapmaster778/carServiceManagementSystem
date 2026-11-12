package com.example.repairestimates;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstimateRequest(@NotBlank @Size(max = 1000) String issueDescription) {
}
