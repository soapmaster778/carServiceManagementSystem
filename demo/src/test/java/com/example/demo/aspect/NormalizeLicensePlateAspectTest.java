package com.example.demo.aspect;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormalizeLicensePlateAspectTest {
    @Mock
    private ProceedingJoinPoint joinPoint;

    private final NormalizeLicensePlateAspect aspect = new NormalizeLicensePlateAspect();

    @Test
    void normalizesDtoAtAnyArgumentPositionAndPreservesOtherFields() throws Throwable {
        RepairOrderDto input = dto("  ki\t1234\u00a0bb  ");
        Object[] originalArguments = {5L, input};
        Object result = new Object();
        when(joinPoint.getArgs()).thenReturn(originalArguments);
        when(joinPoint.proceed(any(Object[].class))).thenReturn(result);

        assertThat(aspect.normalize(joinPoint)).isSameAs(result);

        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(joinPoint).proceed(captor.capture());
        assertThat(captor.getValue()[0]).isEqualTo(5L);
        RepairOrderDto normalized = (RepairOrderDto) captor.getValue()[1];
        assertThat(normalized.licensePlate()).isEqualTo("KI1234BB");
        assertThat(normalized).usingRecursiveComparison().ignoringFields("licensePlate").isEqualTo(input);
        assertThat(originalArguments).containsExactly(5L, input);
        assertThat(input.licensePlate()).isEqualTo("  ki\t1234\u00a0bb  ");
    }

    @Test
    void alreadyCanonicalPlateIsUnchanged() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto("AA1234BB")});

        aspect.normalize(joinPoint);

        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(joinPoint).proceed(captor.capture());
        assertThat(captor.getValue()).containsExactly(dto("AA1234BB"));
    }

    @Test
    void nullPlateNullArgumentAndUnrelatedArgumentsPassThrough() throws Throwable {
        Object[] arguments = {null, 7L, dto(null)};
        when(joinPoint.getArgs()).thenReturn(arguments);

        aspect.normalize(joinPoint);

        verify(joinPoint).proceed(arguments);
    }

    @Test
    void methodsWithoutArgumentsStillProceed() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        aspect.normalize(joinPoint);

        verify(joinPoint).proceed(new Object[0]);
    }

    @Test
    void propagatesBusinessExceptionWithoutRetry() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto("aa1234bb")});
        IllegalStateException failure = new IllegalStateException("Cannot save order");
        when(joinPoint.proceed(any(Object[].class))).thenThrow(failure);

        assertThatThrownBy(() -> aspect.normalize(joinPoint)).isSameAs(failure);
        verify(joinPoint).proceed(any(Object[].class));
    }

    private RepairOrderDto dto(String plate) {
        return new RepairOrderDto(5L, "Customer", "+380501112233", "Toyota", "Corolla",
                plate, "Diagnostics", RepairStatus.NEW, LocalDateTime.of(2026, 1, 1, 9, 0));
    }
}
