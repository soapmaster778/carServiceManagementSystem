package com.example.repairestimates;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class RepairCatalog {
    private final Map<String, RepairInfo> services = Map.of(
            "brakes", new RepairInfo("brakes", "1500.00", "800.00", 90),
            "oil", new RepairInfo("oil", "1200.00", "500.00", 45),
            "diagnostics", new RepairInfo("diagnostics", "0.00", "600.00", 30)
    );

    public Optional<RepairInfo> find(String code) { return Optional.ofNullable(services.get(code)); }
}
