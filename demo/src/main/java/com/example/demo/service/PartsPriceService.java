package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;

import java.math.BigDecimal;

public interface PartsPriceService {
    BigDecimal getPartsPrice(RepairOrderDto order);
}
