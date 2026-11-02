package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.support.TestAdminCredentials;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class AdminCsvExportIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void exportsAreAdminOnlyAndInventoryCsvIsFormulaSafeAndThresholdFiltered() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String customerEmail = "csv-" + suffix + "@shopit.test";
        String customerPassword = "Unique-Csv-" + suffix + "!";

        String registration = mapper.createObjectNode()
                .put("name", "CSV Test Customer")
                .put("email", customerEmail)
                .put("password", customerPassword)
                .toString();
        MvcResult customer = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated())
                .andReturn();
        String customerToken = mapper.readTree(customer.getResponse().getContentAsString())
                .get("token")
                .asText();

        mvc.perform(get("/api/admin/orders/export.csv"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/orders/export.csv")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/orders/export.csv")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"shopit-orders.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("\"Order ID\"")));

        String unsafeName = "=HYPERLINK(\"https://example.invalid\") " + suffix;
        String product = mapper.createObjectNode()
                .put("name", unsafeName)
                .put("description", "CSV formula injection regression")
                .put("price", 99.00)
                .put("stock", 4)
                .put("active", true)
                .toString();
        mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(product))
                .andExpect(status().isCreated());

        MvcResult inventory = mvc.perform(get("/api/admin/inventory/export.csv")
                        .param("threshold", "4")
                        .param("sort", "stock")
                        .param("direction", "asc")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition", "attachment; filename=\"shopit-inventory.csv\""))
                .andReturn();
        String csv = inventory.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(csv).contains("'=HYPERLINK");
        org.assertj.core.api.Assertions.assertThat(csv).contains(suffix);

        MvcResult filteredOut = mvc.perform(get("/api/admin/inventory/export.csv")
                        .param("threshold", "3")
                        .param("sort", "stock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        org.assertj.core.api.Assertions.assertThat(filteredOut.getResponse().getContentAsString())
                .doesNotContain(suffix);

        mvc.perform(get("/api/admin/inventory/export.csv")
                        .param("threshold", "1000001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    private String login(String email, String password) throws Exception {
        String body = mapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .toString();
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
