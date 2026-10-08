package com.example.demo.exception;

public class RepairOrderNotFoundException extends RuntimeException {
    public RepairOrderNotFoundException(Long id) {
        super("Repair order with id " + id + " was not found");
    }
}
