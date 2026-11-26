package com.example.demo.controller;

import com.example.demo.repairinfo.LaborPriceLookup;
import com.example.demo.repairinfo.PartsPriceLookup;
import com.example.demo.repairinfo.RepairDurationLookup;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repair-services")
public class RepairCatalogLookupController {
    private final PartsPriceLookup parts;
    private final LaborPriceLookup labor;
    private final RepairDurationLookup duration;
    public RepairCatalogLookupController(PartsPriceLookup parts, LaborPriceLookup labor, RepairDurationLookup duration) {
        this.parts = parts; this.labor = labor; this.duration = duration;
    }
    @GetMapping("/{code}/parts-price")
    public Map<String, BigDecimal> parts(@PathVariable String code) { return Map.of("partsPrice", parts.getPartsPrice(code)); }
    @GetMapping("/{code}/labor-price")
    public Map<String, BigDecimal> labor(@PathVariable String code) { return Map.of("laborPrice", labor.getLaborPrice(code)); }
    @GetMapping("/{code}/duration")
    public Map<String, Integer> duration(@PathVariable String code) { return Map.of("durationMinutes", duration.getDurationMinutes(code)); }
}
