package com.example.CommerzaGlobalBackend.seller;

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
import com.example.CommerzaGlobalBackend.seller.repository.SellerBankAccountRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerBusinessDetailsRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerKycDetailsRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerOtpRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerPickupAddressRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SellerRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerBusinessDetailsRepository businessRepository;

    @Autowired
    private SellerKycDetailsRepository kycRepository;

    @Autowired
    private SellerBankAccountRepository bankRepository;

    @Autowired
    private SellerPickupAddressRepository pickupRepository;

    @Autowired
    private SellerOtpRepository otpRepository;

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
        otpRepository.deleteAll();
        pickupRepository.deleteAll();
        bankRepository.deleteAll();
        kycRepository.deleteAll();
        businessRepository.deleteAll();
        sellerRepository.deleteAll();
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
    void pickupServiceabilityReturnsHierarchyForAvailablePincode() throws Exception {
        saveHierarchy("679321", true, true, true);

        mockMvc.perform(get("/api/seller/register/pickup-serviceability/679321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.serviceable").value(true))
                .andExpect(jsonPath("$.data.pickupAvailable").value(true))
                .andExpect(jsonPath("$.data.state").value("Kerala"))
                .andExpect(jsonPath("$.data.district").value("Thiruvananthapuram"))
                .andExpect(jsonPath("$.data.town").value("Neyyattinkara Town"));
    }

    @Test
    void pickupServiceabilityReturnsUnavailableForMissingPincode() throws Exception {
        mockMvc.perform(get("/api/seller/register/pickup-serviceability/679321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.serviceable").value(false))
                .andExpect(jsonPath("$.data.pickupAvailable").value(false))
                .andExpect(jsonPath("$.data.message").value("Pickup service is not available for this pincode."));
    }
    @Test
    void sellerCanCompleteRegistrationWithDummyOtpAndPickupPincode() throws Exception {
        saveHierarchy("695121", true, true, true);

        long sellerId = registerAccount("vendor@example.com", "9778070085")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerCode", startsWith("SEL")))
                .andExpect(jsonPath("$.data.applicationStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.accountStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.mobileVerified").value(false))
                .andReturnId();

        mockMvc.perform(post("/api/seller/register/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerId": %d,
                                  "otp": "123456"
                                }
                                """.formatted(sellerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mobileVerified").value(true));

        mockMvc.perform(put("/api/seller/register/{sellerId}/business", sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessName": "ABC Traders Pvt Ltd",
                                  "businessType": "PRIVATE_LIMITED",
                                  "gstRegistered": true,
                                  "gstin": "22AAAAA0000A1Z5",
                                  "businessPan": "ABCDE1234F",
                                  "websiteUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessComplete").value(true));

        mockMvc.perform(put("/api/seller/register/{sellerId}/kyc", sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idProofType": "PAN_CARD",
                                  "idProofNumber": "ABCDE1234F"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kycComplete").value(true));

        mockMvc.perform(put("/api/seller/register/{sellerId}/bank", sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bankName": "HDFC Bank",
                                  "accountHolderName": "Adarsh Dileep",
                                  "accountNumber": "123456789012",
                                  "confirmAccountNumber": "123456789012",
                                  "ifscCode": "HDFC0001234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bankComplete").value(true));

        mockMvc.perform(post("/api/seller/register/{sellerId}/pickup-address", sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locationName": "Main Warehouse",
                                  "contactPerson": "Rahul",
                                  "contactPhone": "9876543210",
                                  "address": "Building 12, Main Road",
                                  "pincode": "695121"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pickupComplete").value(true))
                .andExpect(jsonPath("$.data.pickupAddress.state").value("Kerala"))
                .andExpect(jsonPath("$.data.pickupAddress.district").value("Thiruvananthapuram"))
                .andExpect(jsonPath("$.data.pickupAddress.town").value("Neyyattinkara Town"));

        mockMvc.perform(post("/api/seller/register/{sellerId}/submit", sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Seller application submitted successfully."))
                .andExpect(jsonPath("$.data.sellerCode", startsWith("SEL")))
                .andExpect(jsonPath("$.data.applicationStatus").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.accountStatus").value("PENDING"));
    }

    @Test
    void sellerAccountRegistrationRejectsDuplicateEmailAndMobile() throws Exception {
        registerAccount("vendor@example.com", "9778070085")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        registerAccount("vendor@example.com", "9999999999")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already registered."));

        registerAccount("other@example.com", "9778070085")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mobile number is already registered."));
    }

    @Test
    void dummyOtpRequiresSixNumericDigits() throws Exception {
        long sellerId = registerAccount("vendor@example.com", "9778070085")
                .andExpect(status().isOk())
                .andReturnId();

        mockMvc.perform(post("/api/seller/register/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerId": %d,
                                  "otp": "12345A"
                                }
                                """.formatted(sellerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Enter a valid 6 digit OTP."));
    }

    @Test
    void pickupAddressRejectsUnavailablePincode() throws Exception {
        saveHierarchy("695121", true, false, true);
        long sellerId = registerAccount("vendor@example.com", "9778070085")
                .andExpect(status().isOk())
                .andReturnId();

        mockMvc.perform(post("/api/seller/register/{sellerId}/pickup-address", sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locationName": "Main Warehouse",
                                  "contactPerson": "Rahul",
                                  "contactPhone": "9876543210",
                                  "address": "Building 12, Main Road",
                                  "pincode": "695121"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PICKUP_NOT_AVAILABLE"))
                .andExpect(jsonPath("$.message").value("Pickup service is not available for this pincode."));
    }

    @Test
    void finalSubmissionRequiresAllSectionsComplete() throws Exception {
        long sellerId = registerAccount("vendor@example.com", "9778070085")
                .andExpect(status().isOk())
                .andReturnId();

        mockMvc.perform(post("/api/seller/register/{sellerId}/submit", sellerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mobile verification is required before submission."));
    }

    private ResultWithId registerAccount(String email, String mobile) throws Exception {
        return new ResultWithId(mockMvc.perform(post("/api/seller/register/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Adarsh Dileep",
                                  "email": "%s",
                                  "mobile": "%s",
                                  "password": "Secret123!",
                                  "confirmPassword": "Secret123!",
                                  "termsAccepted": true
                                }
                                """.formatted(email, mobile)))
);
    }

    private SavedHierarchy saveHierarchy(String pincodeValue, boolean active, boolean serviceable, boolean pickupAvailable) {
        GeoState state = stateRepository.save(GeoState.builder().name("Kerala").code("KL").active(active).build());
        GeoZone zone = zoneRepository.save(GeoZone.builder().name("South Zone").code("KL-SOUTH").state(state).active(active).build());
        GeoDistrict district = districtRepository.save(GeoDistrict.builder().name("Thiruvananthapuram").code("TVM").zone(zone).active(active).build());
        GeoTaluk taluk = talukRepository.save(GeoTaluk.builder().name("Neyyattinkara Taluk").district(district).active(active).build());
        GeoTown town = townRepository.save(GeoTown.builder().name("Neyyattinkara Town").taluk(taluk).active(active).build());
        GeoPincode pincode = pincodeRepository.save(GeoPincode.builder().pincode(pincodeValue).town(town).active(active).serviceable(serviceable).pickupAvailable(pickupAvailable).deliveryAvailable(true).build());
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



