package com.example.demo.aspect;

import com.example.demo.exception.WorkshopReadOnlyException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(300)
public class WorkshopWritableAspect {
    private final boolean readOnly;

    public WorkshopWritableAspect(@Value("${workshop.maintenance.read-only:false}") boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Around("@annotation(com.example.demo.annotation.WorkshopWritable)")
    public Object checkWritable(ProceedingJoinPoint joinPoint) throws Throwable {
        if (readOnly) {
            throw new WorkshopReadOnlyException();
        }
        return joinPoint.proceed();
    }
}
