package com.example.demo.aspect;

import com.example.demo.entity.RepairOrder;
import com.example.demo.repository.RepairOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "workshop.maintenance.read-only=true")
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class WorkshopMaintenanceIntegrationTest {
    private static final String VALID_ORDER = """
            {
              "customerName": "Customer", "phone": "+380501112233",
              "carBrand": "Toyota", "carModel": "Corolla",
              "licensePlate": "AA1234BB", "issueDescription": "Diagnostics",
              "status": "NEW"
            }
            """;

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RepairOrderRepository repository;

    @Test
    void maintenanceRejectsAllMutationsWith503BeforeAnyDatabaseAccess() throws Exception {
        mvc.perform(post("/api/repair-orders").contentType("application/json").content(VALID_ORDER))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value(
                        "Workshop is in maintenance read-only mode. Please try again later."));
        mvc.perform(put("/api/repair-orders/1").contentType("application/json").content(VALID_ORDER))
                .andExpect(status().isServiceUnavailable());
        mvc.perform(delete("/api/repair-orders/1"))
                .andExpect(status().isServiceUnavailable());

        verifyNoInteractions(repository);
    }

    @Test
    void readsRemainAvailableDuringMaintenance() throws Exception {
        RepairOrder order = new RepairOrder();
        order.setId(1L);
        order.setLicensePlate("AA1234BB");
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        mvc.perform(get("/api/repair-orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("AA1234BB"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void authorizationStillRunsBeforeMaintenanceGuard() throws Exception {
        mvc.perform(delete("/api/repair-orders/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository);
    }
}
