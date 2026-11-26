package com.example.demo.repairinfo;

public class RepairInfoNotFoundException extends RuntimeException {
    public RepairInfoNotFoundException(String code) { super("Repair service not found: " + code); }
}
