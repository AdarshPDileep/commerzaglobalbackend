package com.example.CommerzaGlobalBackend.ratecard;

import com.example.CommerzaGlobalBackend.ratecard.repository.RateCardRepository;
import com.example.CommerzaGlobalBackend.ratecard.repository.SurchargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateCardRepository rateCardRepository;

    @Autowired
    private SurchargeRepository surchargeRepository;

    @BeforeEach
    void cleanDatabase() {
        surchargeRepository.deleteAll();
        rateCardRepository.deleteAll();
    }

    @Test
    void adminCanCreateListEditToggleAndDeleteRateCard() throws Exception {
        long id = createRateCard("Standard B2B")
                .andExpect(jsonPath("$.data.code").value("RC0001"))
                .andExpect(jsonPath("$.data.name").value("Standard B2B"))
                .andExpect(jsonPath("$.data.type").value("DEFAULT"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.applicableTo").value("All Sellers"))
                .andExpect(jsonPath("$.data.slabs[0].scope").value("LOCAL"))
                .andReturnId();

        mockMvc.perform(get("/api/admin/rate-cards").param("search", "standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value((int) id));

        mockMvc.perform(get("/api/admin/rate-cards/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slabs.length()").value(2));

        mockMvc.perform(put("/api/admin/rate-cards/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rateCardPayload("Express Default", "DEFAULT", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("RC0001"))
                .andExpect(jsonPath("$.data.name").value("Express Default"))
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(patch("/api/admin/rate-cards/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(delete("/api/admin/rate-cards/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) id));

        mockMvc.perform(get("/api/admin/rate-cards/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotCreateOverlappingSlabsForSameScope() throws Exception {
        mockMvc.perform(post("/api/admin/rate-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Broken Slabs",
                                  "type": "DEFAULT",
                                  "active": true,
                                  "slabs": [
                                    { "minWeightKg": 0, "maxWeightKg": 2, "scope": "LOCAL", "rate": 100 },
                                    { "minWeightKg": 1, "maxWeightKg": 3, "scope": "LOCAL", "rate": 150 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Weight slabs cannot overlap for the same pricing scope."));
    }

    @Test
    void adminCanManageSurcharges() throws Exception {
        String response = mockMvc.perform(post("/api/admin/surcharges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Fuel Charge",
                                  "type": "PERCENTAGE",
                                  "value": 5,
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Fuel Charge"))
                .andExpect(jsonPath("$.data.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = extractId(response);

        mockMvc.perform(get("/api/admin/surcharges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value((int) id));

        mockMvc.perform(put("/api/admin/surcharges/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "COD Fee",
                                  "type": "FIXED",
                                  "value": 30,
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("COD Fee"))
                .andExpect(jsonPath("$.data.type").value("FIXED"));

        mockMvc.perform(patch("/api/admin/surcharges/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(delete("/api/admin/surcharges/{id}", id))
                .andExpect(status().isOk());
    }

    private ResultWithId createRateCard(String name) throws Exception {
        return new ResultWithId(mockMvc.perform(post("/api/admin/rate-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rateCardPayload(name, "DEFAULT", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)));
    }

    private String rateCardPayload(String name, String type, boolean active) {
        return """
                {
                  "name": "%s",
                  "type": "%s",
                  "active": %s,
                  "slabs": [
                    { "minWeightKg": 0, "maxWeightKg": 0.5, "scope": "LOCAL", "rate": 50 },
                    { "minWeightKg": 0.5, "maxWeightKg": 1, "scope": "REGIONAL", "rate": 70 }
                  ]
                }
                """.formatted(name, type, active);
    }

    private long extractId(String response) {
        String marker = "\"id\":";
        int start = response.indexOf(marker) + marker.length();
        int end = response.indexOf(",", start);
        return Long.parseLong(response.substring(start, end).trim());
    }

    private class ResultWithId {
        private final org.springframework.test.web.servlet.ResultActions actions;

        private ResultWithId(org.springframework.test.web.servlet.ResultActions actions) {
            this.actions = actions;
        }

        private ResultWithId andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            actions.andExpect(matcher);
            return this;
        }

        private long andReturnId() throws Exception {
            return extractId(actions.andReturn().getResponse().getContentAsString());
        }
    }
}
