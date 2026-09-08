package com.example.CommerzaGlobalBackend.geography;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GeographyControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        pincodeRepository.deleteAll();
        townRepository.deleteAll();
        talukRepository.deleteAll();
        districtRepository.deleteAll();
        zoneRepository.deleteAll();
        stateRepository.deleteAll();
    }

    @Test
    void publicServiceabilityReturnsFullHierarchyForServiceablePincode() throws Exception {
        saveHierarchy(true, true);

        mockMvc.perform(get("/api/geography/serviceability/695121"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pincode").value("695121"))
                .andExpect(jsonPath("$.data.serviceable").value(true))
                .andExpect(jsonPath("$.data.state").value("Kerala"))
                .andExpect(jsonPath("$.data.zone").value("South Zone"))
                .andExpect(jsonPath("$.data.district").value("Thiruvananthapuram"))
                .andExpect(jsonPath("$.data.taluk").value("Neyyattinkara Taluk"))
                .andExpect(jsonPath("$.data.town").value("Neyyattinkara Town"));
    }

    @Test
    void publicServiceabilityIsFalseWhenParentIsInactive() throws Exception {
        saveHierarchy(false, true);

        mockMvc.perform(get("/api/geography/serviceability/695121"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.serviceable").value(false))
                .andExpect(jsonPath("$.data.message").value("Delivery is currently unavailable."));
    }

    @Test
    void adminStatsReturnsMasterDataCounts() throws Exception {
        saveHierarchy(true, true);

        mockMvc.perform(get("/api/admin/geography/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.states").value(1))
                .andExpect(jsonPath("$.data.zones").value(1))
                .andExpect(jsonPath("$.data.districts").value(1))
                .andExpect(jsonPath("$.data.taluks").value(1))
                .andExpect(jsonPath("$.data.towns").value(1))
                .andExpect(jsonPath("$.data.totalPincodes").value(1))
                .andExpect(jsonPath("$.data.serviceablePincodes").value(1));
    }


    @Test
    void adminCanViewUpdateAndDeleteUnusedState() throws Exception {
        GeoState state = stateRepository.save(GeoState.builder()
                .name("Kerala")
                .code("KL")
                .active(true)
                .build());

        mockMvc.perform(get("/api/admin/geography/states/{id}", state.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Kerala"))
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(put("/api/admin/geography/states/{id}", state.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Kerala Updated",
                                  "code": "KLU",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Kerala Updated"))
                .andExpect(jsonPath("$.data.code").value("KLU"));

        mockMvc.perform(delete("/api/admin/geography/states/{id}", state.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/admin/geography/states/{id}", state.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminDeleteStateIsBlockedWhenChildZonesExist() throws Exception {
        GeoState state = saveHierarchy(true, true).state();

        mockMvc.perform(delete("/api/admin/geography/states/{id}", state.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GEOGRAPHY_IN_USE"))
                .andExpect(jsonPath("$.message").value("This state cannot be deleted because dependent records exist."))
                .andExpect(jsonPath("$.dependencies.zones").value(1));

        mockMvc.perform(get("/api/admin/geography/states/{id}", state.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void adminDeleteTownIsBlockedWhenPincodesExist() throws Exception {
        GeoTown town = saveHierarchy(true, true).town();

        mockMvc.perform(delete("/api/admin/geography/towns/{id}", town.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GEOGRAPHY_IN_USE"))
                .andExpect(jsonPath("$.message").value("This town cannot be deleted because dependent records exist."))
                .andExpect(jsonPath("$.dependencies.pincodes").value(1))
                .andExpect(jsonPath("$.dependencies.branches").value(0))
                .andExpect(jsonPath("$.dependencies.hubs").value(0));

        mockMvc.perform(get("/api/admin/geography/towns/{id}", town.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void adminCanViewAndDeletePincodeById() throws Exception {
        GeoPincode pincode = saveHierarchy(true, true).pincode();

        mockMvc.perform(get("/api/admin/geography/pincodes/id/{id}", pincode.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pincode").value("695121"))
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(delete("/api/admin/geography/pincodes/{id}", pincode.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/admin/geography/pincodes/id/{id}", pincode.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void creatingPincodeRejectsInvalidIndianPincode() throws Exception {
        GeoTown town = saveHierarchy(true, true).town();

        mockMvc.perform(post("/api/admin/geography/pincodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pincode": "01234A",
                                  "townId": %d,
                                  "serviceable": true
                                }
                                """.formatted(town.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private SavedHierarchy saveHierarchy(boolean townActive, boolean pincodeServiceable) {
        GeoState state = stateRepository.save(GeoState.builder()
                .name("Kerala")
                .code("KL")
                .active(true)
                .build());
        GeoZone zone = zoneRepository.save(GeoZone.builder()
                .name("South Zone")
                .code("KL-SOUTH")
                .state(state)
                .active(true)
                .build());
        GeoDistrict district = districtRepository.save(GeoDistrict.builder()
                .name("Thiruvananthapuram")
                .code("TVM")
                .zone(zone)
                .active(true)
                .build());
        GeoTaluk taluk = talukRepository.save(GeoTaluk.builder()
                .name("Neyyattinkara Taluk")
                .district(district)
                .active(true)
                .build());
        GeoTown town = townRepository.save(GeoTown.builder()
                .name("Neyyattinkara Town")
                .taluk(taluk)
                .active(townActive)
                .build());
        GeoPincode pincode = pincodeRepository.save(GeoPincode.builder()
                .pincode("695121")
                .town(town)
                .serviceable(pincodeServiceable)
                .pickupAvailable(true)
                .deliveryAvailable(true)
                .codAvailable(true)
                .prepaidAvailable(true)
                .reversePickupAvailable(true)
                .active(true)
                .build());

        return new SavedHierarchy(state, town, pincode);
    }

    private record SavedHierarchy(GeoState state, GeoTown town, GeoPincode pincode) {
    }
}



