package com.example.repairestimates;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepairEstimateServiceTest {
    @Test
    void combinesPartsAndLaborCosts() {
        EstimateTasks tasks = mock(EstimateTasks.class);
        when(tasks.calculatePartsCost("Engine noise")).thenReturn(CompletableFuture.completedFuture(BigDecimal.valueOf(500)));
        when(tasks.calculateLaborCost("Engine noise")).thenReturn(CompletableFuture.completedFuture(BigDecimal.valueOf(700)));

        EstimateResponse response = new RepairEstimateService(tasks)
                .calculate(new EstimateRequest("Engine noise"))
                .join();

        assertThat(response.totalCost()).isEqualByComparingTo("1200");
        assertThat(response.status()).isEqualTo(EstimateStatus.READY);
    }
}
