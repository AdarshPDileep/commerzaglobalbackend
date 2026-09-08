package com.example.CommerzaGlobalBackend.network;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NetworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void adminCanCreateNodeMapPincodesCreateRouteAndViewStats() throws Exception {
        SavedHierarchy origin = saveHierarchy("Kerala", "South Zone", "Thiruvananthapuram", "Neyyattinkara", "Neyyattinkara", "695121");
        SavedHierarchy destination = saveHierarchy("Karnataka", "Bengaluru Zone", "Bengaluru Urban", "Bengaluru South", "Jayanagar", "560041");

        long originNodeId = createNode("Thiruvananthapuram Central Hub", "HUB", 5000, origin);
        long destinationNodeId = createNode("Jayanagar Branch", "BRANCH", 1200, destination);

        mockMvc.perform(post("/api/admin/network/nodes/{nodeId}/pincodes", originNodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pincodeIds": [%d],
                                  "serviceType": "BOTH"
                                }
                                """.formatted(origin.pincode().getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].pincode").value("695121"))
                .andExpect(jsonPath("$.data[0].serviceType").value("BOTH"));

        mockMvc.perform(post("/api/admin/network/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originNodeId": %d,
                                  "destinationNodeId": %d,
                                  "distanceKm": 220,
                                  "transitHours": 6,
                                  "dailyCapacity": 3000
                                }
                                """.formatted(originNodeId, destinationNodeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeCode").value("RTE0001"))
                .andExpect(jsonPath("$.data.origin.id").value((int) originNodeId))
                .andExpect(jsonPath("$.data.destination.id").value((int) destinationNodeId));

        mockMvc.perform(get("/api/admin/network/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalHubs").value(1))
                .andExpect(jsonPath("$.data.totalBranches").value(1))
                .andExpect(jsonPath("$.data.activeRoutes").value(1))
                .andExpect(jsonPath("$.data.totalCapacity").value(6200));
    }

    @Test
    void adminDeleteNodeIsBlockedWhenServiceAreaExistsAndCanDisableInstead() throws Exception {
        SavedHierarchy hierarchy = saveHierarchy("Kerala", "South Zone", "Thiruvananthapuram", "Neyyattinkara", "Neyyattinkara", "695121");
        long nodeId = createNode("Neyyattinkara Branch", "BRANCH", 900, hierarchy);

        mockMvc.perform(post("/api/admin/network/nodes/{nodeId}/pincodes", nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pincodeIds": [%d],
                                  "serviceType": "DELIVERY"
                                }
                                """.formatted(hierarchy.pincode().getId())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/network/nodes/{id}", nodeId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NETWORK_NODE_IN_USE"))
                .andExpect(jsonPath("$.dependencies.pincodes").value(1))
                .andExpect(jsonPath("$.dependencies.routes").value(0));

        mockMvc.perform(patch("/api/admin/network/nodes/{id}/status", nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    private long createNode(String name, String type, int capacity, SavedHierarchy hierarchy) throws Exception {
        String response = mockMvc.perform(post("/api/admin/network/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "dailyCapacity": %d,
                                  "managerUserId": 12,
                                  "address": "Main Road",
                                  "stateId": %d,
                                  "zoneId": %d,
                                  "districtId": %d,
                                  "talukId": %d,
                                  "townId": %d,
                                  "pincodeId": %d
                                }
                                """.formatted(name, type, capacity, hierarchy.state().getId(), hierarchy.zone().getId(), hierarchy.district().getId(), hierarchy.taluk().getId(), hierarchy.town().getId(), hierarchy.pincode().getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String marker = "\"id\":";
        int start = response.indexOf(marker) + marker.length();
        int end = response.indexOf(",", start);
        return Long.parseLong(response.substring(start, end).trim());
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

    private record SavedHierarchy(GeoState state, GeoZone zone, GeoDistrict district, GeoTaluk taluk, GeoTown town, GeoPincode pincode) {
    }
}
