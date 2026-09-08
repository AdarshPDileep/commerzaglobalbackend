package com.example.CommerzaGlobalBackend.commission;

import com.example.CommerzaGlobalBackend.commission.repository.CommissionPayoutConfigRepository;
import com.example.CommerzaGlobalBackend.commission.repository.CommissionRuleRepository;
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
class CommissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommissionRuleRepository commissionRuleRepository;

    @Autowired
    private CommissionPayoutConfigRepository payoutConfigRepository;

    @BeforeEach
    void cleanDatabase() {
        payoutConfigRepository.deleteAll();
        commissionRuleRepository.deleteAll();
    }

    @Test
    void adminCanCreateListEditToggleAndDeleteCommissionRule() throws Exception {
        long id = createRule("STATE", "PERCENTAGE", 15, "ALL_SHIPMENTS", null, "2026-09-08", null, true)
                .andExpect(jsonPath("$.data.code").value("COM0001"))
                .andExpect(jsonPath("$.data.franchiseLevel").value("STATE"))
                .andExpect(jsonPath("$.data.commissionType").value("PERCENTAGE"))
                .andExpect(jsonPath("$.data.value").value(15))
                .andExpect(jsonPath("$.data.conditionType").value("ALL_SHIPMENTS"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturnId();

        mockMvc.perform(get("/api/admin/commissions/rules")
                        .param("search", "COM0001")
                        .param("status", "active")
                        .param("level", "STATE")
                        .param("type", "PERCENTAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value((int) id));

        mockMvc.perform(get("/api/admin/commissions/rules/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) id));

        mockMvc.perform(put("/api/admin/commissions/rules/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload("TALUK", "FLAT", 25, "COD_ONLY", null, "2026-10-01", "2026-12-31", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("COM0001"))
                .andExpect(jsonPath("$.data.franchiseLevel").value("TALUK"))
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(patch("/api/admin/commissions/rules/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(delete("/api/admin/commissions/rules/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) id));

        mockMvc.perform(get("/api/admin/commissions/rules/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotCreateInvalidOrConflictingCommissionRules() throws Exception {
        mockMvc.perform(post("/api/admin/commissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload("STATE", "PERCENTAGE", 150, "ALL_SHIPMENTS", null, "2026-09-08", null, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Percentage commission cannot exceed 100."));

        mockMvc.perform(post("/api/admin/commissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload("STATE", "FLAT", 10, "VOLUME_TARGET", null, "2026-09-08", null, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Minimum volume is required for volume target commission rules."));

        mockMvc.perform(post("/api/admin/commissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload("STATE", "PERCENTAGE", 10, "ALL_SHIPMENTS", null, "2026-12-31", "2026-09-08", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Effective to date cannot be before effective from date."));

        createRule("STATE", "PERCENTAGE", 15, "ALL_SHIPMENTS", null, "2026-09-08", "2026-12-31", true)
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/commissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload("STATE", "PERCENTAGE", 18, "ALL_SHIPMENTS", null, "2026-10-01", null, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("An active commission rule already exists for this franchise level, condition, and effective period."));
    }

    @Test
    void adminCanLoadAndSavePayoutConfig() throws Exception {
        mockMvc.perform(get("/api/admin/commissions/payout-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.frequency").value("WEEKLY"))
                .andExpect(jsonPath("$.data.cutoffDay").value("SUNDAY"))
                .andExpect(jsonPath("$.data.processingDays").value(2))
                .andExpect(jsonPath("$.data.minimumPayout").value(500))
                .andExpect(jsonPath("$.data.bankTransferMode").value("NEFT"))
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(put("/api/admin/commissions/payout-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "frequency": "MONTHLY",
                                  "cutoffDay": "LAST_DAY_OF_MONTH",
                                  "processingDays": 4,
                                  "minimumPayout": 1000,
                                  "bankTransferMode": "RTGS",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.data.cutoffDay").value("LAST_DAY_OF_MONTH"))
                .andExpect(jsonPath("$.data.minimumPayout").value(1000))
                .andExpect(jsonPath("$.data.bankTransferMode").value("RTGS"));
    }

    private ResultWithId createRule(String level, String type, int value, String condition, Integer minimumVolume,
                                    String effectiveFrom, String effectiveTo, boolean active) throws Exception {
        return new ResultWithId(mockMvc.perform(post("/api/admin/commissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload(level, type, value, condition, minimumVolume, effectiveFrom, effectiveTo, active)))
                .andExpect(jsonPath("$.success").value(true)));
    }

    private String rulePayload(String level, String type, int value, String condition, Integer minimumVolume,
                               String effectiveFrom, String effectiveTo, boolean active) {
        return """
                {
                  "franchiseLevel": "%s",
                  "commissionType": "%s",
                  "value": %s,
                  "conditionType": "%s",
                  "minimumVolume": %s,
                  "effectiveFrom": %s,
                  "effectiveTo": %s,
                  "active": %s
                }
                """.formatted(
                level,
                type,
                value,
                condition,
                minimumVolume == null ? "null" : minimumVolume,
                effectiveFrom == null ? "null" : "\"" + effectiveFrom + "\"",
                effectiveTo == null ? "null" : "\"" + effectiveTo + "\"",
                active
        );
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
