package com.example.demo.exception;

public class WorkshopReadOnlyException extends RuntimeException {
    public WorkshopReadOnlyException() {
        super("Workshop is in maintenance read-only mode. Please try again later.");
    }
}
