package com.daniil.booksonline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfTestcontainersAvailable
class BooksOnlineApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldBrowseProductsAndGetProductById() throws Exception {
        String response = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode products = objectMapper.readTree(response);
        assertThat(products).isNotEmpty();
        Long productId = products.get(0).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.intValue()))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldCreateTrackAndListOrders() throws Exception {
        JsonNode products = fetchProducts();
        Long firstProductId = products.get(0).get("id").asLong();
        Long secondProductId = products.get(1).get("id").asLong();

        int firstStockBefore = products.get(0).get("stock").asInt();

        Map<String, Object> request = Map.of(
                "items", List.of(
                        Map.of("productId", firstProductId, "quantity", 1),
                        Map.of("productId", secondProductId, "quantity", 2)
                )
        );

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdOrder = objectMapper.readTree(createResponse);
        Long orderId = createdOrder.get("id").asLong();
        assertThat(createdOrder.get("totalPrice").decimalValue()).isPositive();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.intValue()))
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.intValue()));

        mockMvc.perform(get("/api/products/{id}", firstProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(firstStockBefore - 1));
    }

    @Test
    void shouldRejectPurchaseWhenRequestedQuantityExceedsStock() throws Exception {
        JsonNode products = fetchProducts();
        Long productId = products.get(0).get("id").asLong();
        int stockBefore = products.get(0).get("stock").asInt();

        Map<String, Object> request = Map.of(
                "items", List.of(
                        Map.of("productId", productId, "quantity", stockBefore + 1)
                )
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Insufficient stock")));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(stockBefore));
    }

    private JsonNode fetchProducts() throws Exception {
        String response = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }
}
