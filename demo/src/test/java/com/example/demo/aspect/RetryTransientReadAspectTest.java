package com.example.demo.aspect;

import com.example.demo.annotation.RetryTransientRead;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryTransientReadAspectTest {
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private RetryTransientRead retry;
    @Mock
    private Signature signature;

    private final RetryTransientReadAspect aspect = new RetryTransientReadAspect();

    @Test
    void successfulReadProceedsOnlyOnce() throws Throwable {
        Object result = new Object();
        when(retry.maxAttempts()).thenReturn(3);
        when(joinPoint.proceed()).thenReturn(result);

        assertThat(aspect.retry(joinPoint, retry)).isSameAs(result);
        verify(joinPoint).proceed();
    }

    @Test
    void transientFailureThenSuccessRetriesAndReturnsResult() throws Throwable {
        when(retry.maxAttempts()).thenReturn(3);
        enableRetryLog();
        when(joinPoint.proceed()).thenThrow(transientFailure()).thenReturn("order");

        assertThat(aspect.retry(joinPoint, retry)).isEqualTo("order");
        verify(joinPoint, times(2)).proceed();
    }

    @Test
    void exhaustedAttemptsRethrowLastFailure() throws Throwable {
        when(retry.maxAttempts()).thenReturn(3);
        enableRetryLog();
        var lastFailure = transientFailure();
        when(joinPoint.proceed()).thenThrow(transientFailure(), transientFailure(), lastFailure);

        assertThatThrownBy(() -> aspect.retry(joinPoint, retry)).isSameAs(lastFailure);
        verify(joinPoint, times(3)).proceed();
    }

    @Test
    void oneAttemptDisablesRetries() throws Throwable {
        when(retry.maxAttempts()).thenReturn(1);
        var failure = transientFailure();
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> aspect.retry(joinPoint, retry)).isSameAs(failure);
        verify(joinPoint).proceed();
    }

    @Test
    void businessFailureIsNotRetried() throws Throwable {
        when(retry.maxAttempts()).thenReturn(3);
        var failure = new IllegalArgumentException("Invalid id");
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> aspect.retry(joinPoint, retry)).isSameAs(failure);
        verify(joinPoint).proceed();
    }

    @Test
    void invalidAttemptLimitRejectsCallWithoutProceeding() {
        when(retry.maxAttempts()).thenReturn(0);

        assertThatThrownBy(() -> aspect.retry(joinPoint, retry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxAttempts must be at least 1");
        verifyNoInteractions(joinPoint);
    }

    private void enableRetryLog() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("RepairOrderService.findById(..)");
    }

    private TransientDataAccessResourceException transientFailure() {
        return new TransientDataAccessResourceException("Temporary database failure");
    }
}
