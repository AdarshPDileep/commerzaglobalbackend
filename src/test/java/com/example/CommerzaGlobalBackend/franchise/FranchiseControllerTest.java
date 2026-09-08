package com.example.CommerzaGlobalBackend.franchise;

import com.example.CommerzaGlobalBackend.franchise.repository.FranchiseRepository;
import com.example.CommerzaGlobalBackend.geography.entity.GeoDistrict;
import com.example.CommerzaGlobalBackend.geography.entity.GeoPincode;
import com.example.CommerzaGlobalBackend.geography.entity.GeoState;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTaluk;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTown;
import com.example.CommerzaGlobalBackend.geography.entity.GeoZone;
import com.example.CommerzaGlobalBackend.geography.repository.GeoDistrictRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoPincodeRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoStateRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoTalukRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoTownRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoZoneRepository;
import com.example.CommerzaGlobalBackend.network.repository.NetworkNodePincodeRepository;
import com.example.CommerzaGlobalBackend.network.repository.NetworkNodeRepository;
import com.example.CommerzaGlobalBackend.network.repository.NetworkRouteRepository;
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
class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FranchiseRepository franchiseRepository;

    @Autowired
    private NetworkRouteRepository routeRepository;

    @Autowired
    private NetworkNodePincodeRepository nodePincodeRepository;

    @Autowired
    private NetworkNodeRepository nodeRepository;

    @Autowired
    private GeoStateRepository stateRepository;

    @Autowired
    private GeoZoneRepository zoneRepository;

    @Autowired
    private GeoDistrictRepository districtRepository;

    @Autowired
    private GeoTalukRepository talukRepository;

    @Autowired
    private GeoTownRepository townRepository;

    @Autowired
    private GeoPincodeRepository pincodeRepository;

    @BeforeEach
    void cleanDatabase() {
        franchiseRepository.deleteAll();
        routeRepository.deleteAll();
        nodePincodeRepository.deleteAll();
        nodeRepository.deleteAll();
        pincodeRepository.deleteAll();
        townRepository.deleteAll();
        talukRepository.deleteAll();
        districtRepository.deleteAll();
        zoneRepository.deleteAll();
        stateRepository.deleteAll();
    }

    @Test
    void adminCanCreateListViewUpdateStatusAndDeleteFranchise() throws Exception {
        SavedHierarchy hierarchy = saveHierarchy("Kerala", "South Zone", "Thiruvananthapuram", "Neyyattinkara", "Neyyattinkara", "695121");
        long nodeId = createNode("Neyyattinkara Branch", "BRANCH", hierarchy);

        long franchiseId = createFranchise("Neyyattinkara Logistics", "TOWN", hierarchy, nodeId)
                .andExpect(jsonPath("$.data.code").value("FRN0001"))
                .andExpect(jsonPath("$.data.name").value("Neyyattinkara Logistics"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.networkNode.name").value("Neyyattinkara Branch"))
                .andReturnId();

        mockMvc.perform(get("/api/admin/franchises")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "Neyyattinkara")
                        .param("status", "ACTIVE")
                        .param("type", "TOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value((int) franchiseId))
                .andExpect(jsonPath("$.data.content[0].code").value("FRN0001"));

        mockMvc.perform(get("/api/admin/franchises/{id}", franchiseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ownerName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.data.pincode.pincode").value("695121"));

        mockMvc.perform(put("/api/admin/franchises/{id}", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchisePayload("Neyyattinkara Express", "LOCAL", hierarchy, nodeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("FRN0001"))
                .andExpect(jsonPath("$.data.name").value("Neyyattinkara Express"))
                .andExpect(jsonPath("$.data.type").value("LOCAL"));

        mockMvc.perform(patch("/api/admin/franchises/{id}/status", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        mockMvc.perform(delete("/api/admin/franchises/{id}", franchiseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) franchiseId));

        mockMvc.perform(get("/api/admin/franchises/{id}", franchiseId))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotAssignInactiveOrSortingCenterNode() throws Exception {
        SavedHierarchy hierarchy = saveHierarchy("Kerala", "South Zone", "Thiruvananthapuram", "Neyyattinkara", "Neyyattinkara", "695121");
        long inactiveHubId = createNode("Closed Hub", "HUB", hierarchy);

        mockMvc.perform(patch("/api/admin/network/nodes/{id}/status", inactiveHubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchisePayload("Inactive Hub Franchise", "TOWN", hierarchy, inactiveHubId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Assigned hub/branch must be active."));

        long sortingCenterId = createNode("Sorting Center", "SORTING_CENTER", hierarchy);

        mockMvc.perform(post("/api/admin/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchisePayload("Sorting Franchise", "TOWN", hierarchy, sortingCenterId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Assigned hub/branch must be a hub, branch, or delivery center."));
    }

    private ResultWithId createFranchise(String name, String type, SavedHierarchy hierarchy, long nodeId) throws Exception {
        return new ResultWithId(mockMvc.perform(post("/api/admin/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchisePayload(name, type, hierarchy, nodeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)));
    }

    private long createNode(String name, String type, SavedHierarchy hierarchy) throws Exception {
        String response = mockMvc.perform(post("/api/admin/network/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "dailyCapacity": 900,
                                  "address": "Main Road",
                                  "stateId": %d,
                                  "zoneId": %d,
                                  "districtId": %d,
                                  "talukId": %d,
                                  "townId": %d,
                                  "pincodeId": %d
                                }
                                """.formatted(name, type, hierarchy.state().getId(), hierarchy.zone().getId(), hierarchy.district().getId(), hierarchy.taluk().getId(), hierarchy.town().getId(), hierarchy.pincode().getId())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractId(response);
    }

    private String franchisePayload(String name, String type, SavedHierarchy hierarchy, long nodeId) {
        return """
                {
                  "name": "%s",
                  "type": "%s",
                  "ownerName": "Rahul Sharma",
                  "phone": "9876543210",
                  "email": "rahul@example.com",
                  "address": "Neyyattinkara, Kerala",
                  "stateId": %d,
                  "zoneId": %d,
                  "districtId": %d,
                  "talukId": %d,
                  "townId": %d,
                  "pincodeId": %d,
                  "networkNodeId": %d
                }
                """.formatted(name, type, hierarchy.state().getId(), hierarchy.zone().getId(), hierarchy.district().getId(), hierarchy.taluk().getId(), hierarchy.town().getId(), hierarchy.pincode().getId(), nodeId);
    }

    private SavedHierarchy saveHierarchy(String stateName, String zoneName, String districtName, String talukName, String townName, String pincodeValue) {
        GeoState state = stateRepository.save(GeoState.builder().name(stateName).code(stateName.substring(0, 2).toUpperCase()).active(true).build());
        GeoZone zone = zoneRepository.save(GeoZone.builder().name(zoneName).code(zoneName.substring(0, 2).toUpperCase()).state(state).active(true).build());
        GeoDistrict district = districtRepository.save(GeoDistrict.builder().name(districtName).code(districtName.substring(0, 3).toUpperCase()).zone(zone).active(true).build());
        GeoTaluk taluk = talukRepository.save(GeoTaluk.builder().name(talukName).district(district).active(true).build());
        GeoTown town = townRepository.save(GeoTown.builder().name(townName).taluk(taluk).active(true).build());
        GeoPincode pincode = pincodeRepository.save(GeoPincode.builder().pincode(pincodeValue).town(town).active(true).serviceable(true).pickupAvailable(true).deliveryAvailable(true).build());
        return new SavedHierarchy(state, zone, district, taluk, town, pincode);
    }

    private long extractId(String response) {
        String marker = "\"id\":";
        int start = response.indexOf(marker) + marker.length();
        int end = response.indexOf(",", start);
        return Long.parseLong(response.substring(start, end).trim());
    }

    private record SavedHierarchy(GeoState state, GeoZone zone, GeoDistrict district, GeoTaluk taluk, GeoTown town, GeoPincode pincode) {
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
