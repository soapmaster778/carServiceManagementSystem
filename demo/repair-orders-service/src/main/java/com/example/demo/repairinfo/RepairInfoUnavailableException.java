package com.example.demo.repairinfo;

public class RepairInfoUnavailableException extends RuntimeException {
    public RepairInfoUnavailableException(Throwable cause) { super("Repair catalog is unavailable", cause); }
}
