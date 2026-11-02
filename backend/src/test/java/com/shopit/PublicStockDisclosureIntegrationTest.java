package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
class PublicStockDisclosureIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String adminToken() throws Exception {
        String body = mapper.createObjectNode().put("email", TestAdminCredentials.email())
                .put("password", TestAdminCredentials.password()).toString();
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createProduct(String admin, String name, int stock) throws Exception {
        String body = mapper.createObjectNode().put("name", name)
                .put("description", "Public stock disclosure boundary test")
                .put("price", 10.00).put("stock", stock).put("active", true).toString();
        MvcResult result = mvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void publicStockIsBucketedWhileAdminRetainsExactStock() throws Exception {
        String admin = adminToken();
        long soldOut = createProduct(admin, "Sold out " + UUID.randomUUID(), 0);
        long oneLeft = createProduct(admin, "One left " + UUID.randomUUID(), 1);
        long fiveLeft = createProduct(admin, "Five left " + UUID.randomUUID(), 5);
        long sixLeft = createProduct(admin, "Six left " + UUID.randomUUID(), 6);

        assertPublicStock(soldOut, false, false);
        assertPublicStock(oneLeft, true, true);
        assertPublicStock(fiveLeft, true, true);
        assertPublicStock(sixLeft, true, false);

        mvc.perform(get("/api/admin/products?size=100").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        MvcResult adminResult = mvc.perform(get("/api/admin/products?size=100")
                .header("Authorization", "Bearer " + admin)).andReturn();
        JsonNode page = mapper.readTree(adminResult.getResponse().getContentAsString());
        assertThat(page.get("content")).isNotNull();
        assertThat(page.get("size").asInt()).isEqualTo(100);
        assertThat(findStock(page.get("content"), sixLeft)).isEqualTo(6);
    }

    private void assertPublicStock(long id, boolean inStock, boolean lowStock) throws Exception {
        MvcResult result = mvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inStock").value(inStock))
                .andExpect(jsonPath("$.lowStock").value(lowStock))
                .andExpect(jsonPath("$.stock").doesNotExist())
                .andReturn();
        assertThat(mapper.readTree(result.getResponse().getContentAsString()).has("stock")).isFalse();
    }

    private int findStock(JsonNode products, long id) {
        for (JsonNode product : products) {
            if (product.get("id").asLong() == id) {
                return product.get("stock").asInt();
            }
        }
        throw new AssertionError("Admin product was not found");
    }
}
