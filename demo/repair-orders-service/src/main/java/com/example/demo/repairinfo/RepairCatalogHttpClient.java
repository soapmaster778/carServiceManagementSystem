package com.example.demo.repairinfo;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/repair-catalog")
public interface RepairCatalogHttpClient {
    @GetExchange("/{code}")
    RepairInfo find(@PathVariable("code") String code);
}
