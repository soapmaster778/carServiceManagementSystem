package com.example.demo.aspect;

import com.example.demo.exception.WorkshopReadOnlyException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopWritableAspectTest {
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    void writableWorkshopProceedsAndReturnsUnchangedResult() throws Throwable {
        Object result = new Object();
        when(joinPoint.proceed()).thenReturn(result);

        assertThat(new WorkshopWritableAspect(false).checkWritable(joinPoint)).isSameAs(result);
        verify(joinPoint).proceed();
    }

    @Test
    void maintenanceRejectsMutationWithoutProceeding() {
        assertThatThrownBy(() -> new WorkshopWritableAspect(true).checkWritable(joinPoint))
                .isInstanceOf(WorkshopReadOnlyException.class)
                .hasMessageContaining("maintenance read-only mode");
        verifyNoInteractions(joinPoint);
    }

    @Test
    void writableWorkshopPropagatesBusinessFailure() throws Throwable {
        var failure = new IllegalStateException("Cannot save order");
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> new WorkshopWritableAspect(false).checkWritable(joinPoint))
                .isSameAs(failure);
        verify(joinPoint).proceed();
    }
}
