package com.example.demo.aspect;

import com.example.demo.annotation.RetryTransientRead;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(500)
public class RetryTransientReadAspect {
    private static final Logger log = LoggerFactory.getLogger(RetryTransientReadAspect.class);

    @Around(value = "@annotation(retry)", argNames = "joinPoint,retry")
    public Object retry(ProceedingJoinPoint joinPoint, RetryTransientRead retry) throws Throwable {
        int maxAttempts = retry.maxAttempts();
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        for (int attempt = 1; ; attempt++) {
            try {
                return joinPoint.proceed();
            } catch (TransientDataAccessException exception) {
                if (attempt >= maxAttempts) {
                    throw exception;
                }
                log.warn("Retrying transient database read: method={}, nextAttempt={}, maxAttempts={}",
                        joinPoint.getSignature().toShortString(), attempt + 1, maxAttempts);
            }
        }
    }
}
