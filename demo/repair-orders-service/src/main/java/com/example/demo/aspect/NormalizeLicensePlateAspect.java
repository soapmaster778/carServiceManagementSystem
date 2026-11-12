package com.example.demo.aspect;

import com.example.demo.dto.RepairOrderDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Aspect
@Component
@Order(400)
public class NormalizeLicensePlateAspect {
    @Around("@annotation(com.example.demo.annotation.NormalizeLicensePlate)")
    public Object normalize(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] arguments = joinPoint.getArgs().clone();
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof RepairOrderDto dto && dto.licensePlate() != null) {
                String plate = dto.licensePlate().replaceAll("(?U)\\s+", "").toUpperCase(Locale.ROOT);
                arguments[i] = new RepairOrderDto(dto.id(), dto.customerName(), dto.phone(),
                        dto.carBrand(), dto.carModel(), plate, dto.issueDescription(),
                        dto.status(), dto.createdAt());
            }
        }
        return joinPoint.proceed(arguments);
    }
}
