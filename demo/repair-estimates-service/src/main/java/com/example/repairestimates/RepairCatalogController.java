package com.example.repairestimates;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/repair-catalog")
public class RepairCatalogController {
    private final RepairCatalog catalog;
    public RepairCatalogController(RepairCatalog catalog) { this.catalog = catalog; }
    @GetMapping("/{code}")
    public RepairInfo find(@PathVariable String code) {
        return catalog.find(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repair service not found"));
    }
}
