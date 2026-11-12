package com.example.demo.service;

import com.example.demo.dto.RepairOrderDto;

public interface NotificationService {
    void sendOrderAccepted(RepairOrderDto order);
}
