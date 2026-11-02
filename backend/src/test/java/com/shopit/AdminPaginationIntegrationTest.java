package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("ci")
class AdminPaginationIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void adminProductListingReturnsPagesAndSupportsSearchAndActiveFilter() throws Exception {
        mvc.perform(get("/api/admin/products?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.number").value(0));

        mvc.perform(get("/api/admin/products?page=0&size=20&active=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mvc.perform(get("/api/admin/products?q=seed&page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void adminOrderListingSupportsStatusFilterAndRejectsUnboundedPageSize() throws Exception {
        mvc.perform(get("/api/admin/orders?page=0&size=20&status=PENDING_PAYMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0));

        mvc.perform(get("/api/admin/orders?size=101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/orders")
                        .param("orderId", "999999")
                        .param("email", "seed")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2027-01-01T00:00:00Z"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/orders")
                        .param("from", "2027-01-01T00:00:00Z")
                        .param("to", "2026-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/products?size=101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/products?page=-1")).andExpect(status().isBadRequest());
    }
}
