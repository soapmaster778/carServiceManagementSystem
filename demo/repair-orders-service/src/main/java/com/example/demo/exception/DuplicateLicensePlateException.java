package com.example.demo.exception;

public class DuplicateLicensePlateException extends RuntimeException {
    public DuplicateLicensePlateException(String licensePlate) {
        super("An active order for license plate " + licensePlate + " already exists");
    }
}
