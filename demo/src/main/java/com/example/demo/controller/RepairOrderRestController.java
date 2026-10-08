package com.example.demo.controller;

import com.example.demo.annotation.CurrentUsername;
import com.example.demo.dto.RepairOrderDto;
import com.example.demo.service.RepairOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.demo.annotation.CreatedPost;

@RestController
@RequestMapping("/api/repair-orders")
@RequiredArgsConstructor
public class RepairOrderRestController {
    private final RepairOrderService service;

    @GetMapping
    public List<RepairOrderDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RepairOrderDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @CreatedPost
    public RepairOrderDto create(
            @Valid @RequestBody RepairOrderDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public RepairOrderDto update(@PathVariable Long id, @Valid @RequestBody RepairOrderDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/current-operator")
    public Map<String, String> currentOperator(
            @CurrentUsername String username) {

        return Map.of("username", username);
    }
}
