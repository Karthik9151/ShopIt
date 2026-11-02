package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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
class AdminCustomerSearchIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void customerSearchAndDetailAreAdminOnlyAndNeverExposeAdminAccounts() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String email = "customer-" + suffix + "@shopit.test";
        String password = "Unique-Customer-" + suffix + "!";

        String registerBody = mapper.createObjectNode()
                .put("name", "Customer Search " + suffix)
                .put("email", email)
                .put("password", password)
                .toString();
        MvcResult registered = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode user = mapper.readTree(registered.getResponse().getContentAsString()).get("user");
        long customerId = user.get("id").asLong();
        String customerToken = mapper.readTree(registered.getResponse().getContentAsString())
                .get("token")
                .asText();

        mvc.perform(get("/api/admin/customers?q=" + suffix)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        MvcResult search = mvc.perform(get("/api/admin/customers")
                        .param("q", suffix)
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value(email))
                .andReturn();
        assertThatJsonHasNoSensitiveFields(mapper.readTree(search.getResponse().getContentAsString()));

        mvc.perform(get("/api/admin/customers/" + customerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mvc.perform(get("/api/admin/customers")
                        .param("q", TestAdminCredentials.email())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mvc.perform(get("/api/admin/customers")
                        .param("q", "x".repeat(121))
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

    private void assertThatJsonHasNoSensitiveFields(JsonNode response) {
        for (JsonNode customer : response.get("content")) {
            org.assertj.core.api.Assertions.assertThat(customer.has("passwordHash")).isFalse();
            org.assertj.core.api.Assertions.assertThat(customer.has("role")).isFalse();
        }
    }
}
