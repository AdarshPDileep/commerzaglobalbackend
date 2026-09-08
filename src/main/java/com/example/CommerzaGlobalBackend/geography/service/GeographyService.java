package com.example.CommerzaGlobalBackend.geography.service;

import com.example.CommerzaGlobalBackend.common.exception.DuplicateResourceException;
import com.example.CommerzaGlobalBackend.common.exception.ResourceNotFoundException;
import com.example.CommerzaGlobalBackend.geography.dto.DistrictRequest;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyItemResponse;
import com.example.CommerzaGlobalBackend.geography.dto.GeographySearchResponse;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyStatsResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeDetailsResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeRequest;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeServiceabilityRequest;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeSummaryResponse;
import com.example.CommerzaGlobalBackend.geography.dto.ServiceabilityResponse;
import com.example.CommerzaGlobalBackend.geography.dto.StateRequest;
import com.example.CommerzaGlobalBackend.geography.dto.StateResponse;
import com.example.CommerzaGlobalBackend.geography.dto.TalukRequest;
import com.example.CommerzaGlobalBackend.geography.dto.TownRequest;
import com.example.CommerzaGlobalBackend.geography.dto.ZoneRequest;
import com.example.CommerzaGlobalBackend.geography.entity.GeoDistrict;
import com.example.CommerzaGlobalBackend.geography.entity.GeoPincode;
import com.example.CommerzaGlobalBackend.geography.entity.GeoState;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTaluk;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTown;
import com.example.CommerzaGlobalBackend.geography.entity.GeoZone;
import com.example.CommerzaGlobalBackend.geography.exception.GeographyInUseException;
import com.example.CommerzaGlobalBackend.geography.repository.GeoDistrictRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoPincodeRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoStateRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoTalukRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoTownRepository;
import com.example.CommerzaGlobalBackend.geography.repository.GeoZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GeographyService {

    private static final String PINCODE_PATTERN = "^[1-9][0-9]{5}$";

    private final GeoStateRepository stateRepository;
    private final GeoZoneRepository zoneRepository;
    private final GeoDistrictRepository districtRepository;
    private final GeoTalukRepository talukRepository;
    private final GeoTownRepository townRepository;
    private final GeoPincodeRepository pincodeRepository;

    public GeographyService(GeoStateRepository stateRepository,
                            GeoZoneRepository zoneRepository,
                            GeoDistrictRepository districtRepository,
                            GeoTalukRepository talukRepository,
                            GeoTownRepository townRepository,
                            GeoPincodeRepository pincodeRepository) {
        this.stateRepository = stateRepository;
        this.zoneRepository = zoneRepository;
        this.districtRepository = districtRepository;
        this.talukRepository = talukRepository;
        this.townRepository = townRepository;
        this.pincodeRepository = pincodeRepository;
    }

    public GeographyStatsResponse getStats() {
        return new GeographyStatsResponse(
                stateRepository.count(),
                zoneRepository.count(),
                districtRepository.count(),
                talukRepository.count(),
                townRepository.count(),
                pincodeRepository.count(),
                pincodeRepository.countByServiceableTrue()
        );
    }

    public List<StateResponse> getStates() {
        return stateRepository.findAll().stream().map(this::toStateResponse).toList();
    }


    @Transactional(readOnly = true)
    public GeographyItemResponse getState(Long id) {
        return toItem(findState(id));
    }

    @Transactional(readOnly = true)
    public GeographyItemResponse getZone(Long id) {
        return toItem(findZone(id));
    }

    @Transactional(readOnly = true)
    public GeographyItemResponse getDistrict(Long id) {
        return toItem(findDistrict(id));
    }

    @Transactional(readOnly = true)
    public GeographyItemResponse getTaluk(Long id) {
        return toItem(findTaluk(id));
    }

    @Transactional(readOnly = true)
    public GeographyItemResponse getTown(Long id) {
        return toItem(findTown(id));
    }

    @Transactional(readOnly = true)
    public PincodeDetailsResponse getPincodeById(Long id) {
        return toPincodeDetails(findPincode(id));
    }

    public GeographyItemResponse deleteState(Long id) {
        GeoState state = findState(id);
        Map<String, Long> dependencies = Map.of(
                "zones", zoneRepository.countByStateId(state.getId()),
                "branches", 0L,
                "hubs", 0L
        );
        ensureNoDependencies("state", dependencies);
        GeographyItemResponse response = toItem(state);
        stateRepository.delete(state);
        return response;
    }

    public GeographyItemResponse deleteZone(Long id) {
        GeoZone zone = findZone(id);
        Map<String, Long> dependencies = Map.of(
                "districts", districtRepository.countByZoneId(zone.getId()),
                "branches", 0L,
                "hubs", 0L
        );
        ensureNoDependencies("zone", dependencies);
        GeographyItemResponse response = toItem(zone);
        zoneRepository.delete(zone);
        return response;
    }

    public GeographyItemResponse deleteDistrict(Long id) {
        GeoDistrict district = findDistrict(id);
        Map<String, Long> dependencies = Map.of(
                "taluks", talukRepository.countByDistrictId(district.getId()),
                "branches", 0L,
                "hubs", 0L
        );
        ensureNoDependencies("district", dependencies);
        GeographyItemResponse response = toItem(district);
        districtRepository.delete(district);
        return response;
    }

    public GeographyItemResponse deleteTaluk(Long id) {
        GeoTaluk taluk = findTaluk(id);
        Map<String, Long> dependencies = Map.of(
                "towns", townRepository.countByTalukId(taluk.getId()),
                "branches", 0L,
                "hubs", 0L
        );
        ensureNoDependencies("taluk", dependencies);
        GeographyItemResponse response = toItem(taluk);
        talukRepository.delete(taluk);
        return response;
    }

    public GeographyItemResponse deleteTown(Long id) {
        GeoTown town = findTown(id);
        Map<String, Long> dependencies = Map.of(
                "pincodes", pincodeRepository.countByTownId(town.getId()),
                "branches", 0L,
                "hubs", 0L
        );
        ensureNoDependencies("town", dependencies);
        GeographyItemResponse response = toItem(town);
        townRepository.delete(town);
        return response;
    }

    public PincodeDetailsResponse deletePincode(Long id) {
        GeoPincode pincode = findPincode(id);
        Map<String, Long> dependencies = Map.of(
                "branches", 0L,
                "hubs", 0L
        );
        ensureNoDependencies("pincode", dependencies);
        PincodeDetailsResponse response = toPincodeDetails(pincode);
        pincodeRepository.delete(pincode);
        return response;
    }

    public GeographyItemResponse createState(StateRequest request) {
        String name = required(request.name(), "State name is required.");
        if (stateRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("State already exists.");
        }
        GeoState state = GeoState.builder()
                .name(name)
                .code(normalizeCode(request.code()))
                .active(defaultTrue(request.active()))
                .build();
        return toItem(stateRepository.save(state));
    }

    public GeographyItemResponse updateState(Long id, StateRequest request) {
        GeoState state = findState(id);
        state.setName(required(request.name(), "State name is required."));
        state.setCode(normalizeCode(request.code()));
        if (request.active() != null) state.setActive(request.active());
        return toItem(state);
    }

    public List<GeographyItemResponse> getZonesByState(Long stateId) {
        findState(stateId);
        return zoneRepository.findByStateIdOrderByNameAsc(stateId).stream().map(this::toItem).toList();
    }

    public GeographyItemResponse createZone(ZoneRequest request) {
        GeoState state = findState(requiredId(request.stateId(), "State is required."));
        String name = required(request.name(), "Zone name is required.");
        if (zoneRepository.existsByStateIdAndNameIgnoreCase(state.getId(), name)) {
            throw new DuplicateResourceException("Zone already exists under this state.");
        }
        GeoZone zone = GeoZone.builder()
                .name(name)
                .code(normalizeCode(request.code()))
                .state(state)
                .active(defaultTrue(request.active()))
                .build();
        return toItem(zoneRepository.save(zone));
    }

    public GeographyItemResponse updateZone(Long id, ZoneRequest request) {
        GeoZone zone = findZone(id);
        if (request.stateId() != null && !request.stateId().equals(zone.getState().getId())) {
            zone.setState(findState(request.stateId()));
        }
        zone.setName(required(request.name(), "Zone name is required."));
        zone.setCode(normalizeCode(request.code()));
        if (request.active() != null) zone.setActive(request.active());
        return toItem(zone);
    }

    public List<GeographyItemResponse> getDistrictsByZone(Long zoneId) {
        findZone(zoneId);
        return districtRepository.findByZoneIdOrderByNameAsc(zoneId).stream().map(this::toItem).toList();
    }

    public GeographyItemResponse createDistrict(DistrictRequest request) {
        GeoZone zone = findZone(requiredId(request.zoneId(), "Zone is required."));
        String name = required(request.name(), "District name is required.");
        if (districtRepository.existsByZoneIdAndNameIgnoreCase(zone.getId(), name)) {
            throw new DuplicateResourceException("District already exists under this zone.");
        }
        GeoDistrict district = GeoDistrict.builder()
                .name(name)
                .code(normalizeCode(request.code()))
                .zone(zone)
                .active(defaultTrue(request.active()))
                .build();
        return toItem(districtRepository.save(district));
    }

    public GeographyItemResponse updateDistrict(Long id, DistrictRequest request) {
        GeoDistrict district = findDistrict(id);
        if (request.zoneId() != null && !request.zoneId().equals(district.getZone().getId())) {
            district.setZone(findZone(request.zoneId()));
        }
        district.setName(required(request.name(), "District name is required."));
        district.setCode(normalizeCode(request.code()));
        if (request.active() != null) district.setActive(request.active());
        return toItem(district);
    }

    public List<GeographyItemResponse> getTaluksByDistrict(Long districtId) {
        findDistrict(districtId);
        return talukRepository.findByDistrictIdOrderByNameAsc(districtId).stream().map(this::toItem).toList();
    }

    public GeographyItemResponse createTaluk(TalukRequest request) {
        GeoDistrict district = findDistrict(requiredId(request.districtId(), "District is required."));
        String name = required(request.name(), "Taluk name is required.");
        if (talukRepository.existsByDistrictIdAndNameIgnoreCase(district.getId(), name)) {
            throw new DuplicateResourceException("Taluk already exists under this district.");
        }
        GeoTaluk taluk = GeoTaluk.builder()
                .name(name)
                .district(district)
                .active(defaultTrue(request.active()))
                .build();
        return toItem(talukRepository.save(taluk));
    }

    public GeographyItemResponse updateTaluk(Long id, TalukRequest request) {
        GeoTaluk taluk = findTaluk(id);
        if (request.districtId() != null && !request.districtId().equals(taluk.getDistrict().getId())) {
            taluk.setDistrict(findDistrict(request.districtId()));
        }
        taluk.setName(required(request.name(), "Taluk name is required."));
        if (request.active() != null) taluk.setActive(request.active());
        return toItem(taluk);
    }

    public List<GeographyItemResponse> getTownsByTaluk(Long talukId) {
        findTaluk(talukId);
        return townRepository.findByTalukIdOrderByNameAsc(talukId).stream().map(this::toItem).toList();
    }

    public GeographyItemResponse createTown(TownRequest request) {
        GeoTaluk taluk = findTaluk(requiredId(request.talukId(), "Taluk is required."));
        String name = required(request.name(), "Town name is required.");
        if (townRepository.existsByTalukIdAndNameIgnoreCase(taluk.getId(), name)) {
            throw new DuplicateResourceException("Town already exists under this taluk.");
        }
        GeoTown town = GeoTown.builder()
                .name(name)
                .taluk(taluk)
                .active(defaultTrue(request.active()))
                .build();
        return toItem(townRepository.save(town));
    }

    public GeographyItemResponse updateTown(Long id, TownRequest request) {
        GeoTown town = findTown(id);
        if (request.talukId() != null && !request.talukId().equals(town.getTaluk().getId())) {
            town.setTaluk(findTaluk(request.talukId()));
        }
        town.setName(required(request.name(), "Town name is required."));
        if (request.active() != null) town.setActive(request.active());
        return toItem(town);
    }

    public PageResponse<PincodeSummaryResponse> getPincodesByTown(Long townId, int page, int size) {
        findTown(townId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<GeoPincode> result = pincodeRepository.findByTownIdOrderByPincodeAsc(townId, pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toPincodeSummary).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public PincodeDetailsResponse createPincode(PincodeRequest request) {
        validatePincode(request.pincode());
        if (pincodeRepository.existsByPincode(request.pincode())) {
            throw new DuplicateResourceException("Pincode already exists.");
        }
        GeoTown town = findTown(requiredId(request.townId(), "Town is required."));
        GeoPincode pincode = GeoPincode.builder()
                .pincode(request.pincode())
                .town(town)
                .serviceable(defaultTrue(request.serviceable()))
                .pickupAvailable(defaultTrue(request.pickupAvailable()))
                .deliveryAvailable(defaultTrue(request.deliveryAvailable()))
                .codAvailable(defaultTrue(request.codAvailable()))
                .prepaidAvailable(defaultTrue(request.prepaidAvailable()))
                .reversePickupAvailable(defaultTrue(request.reversePickupAvailable()))
                .active(defaultTrue(request.active()))
                .build();
        return toPincodeDetails(pincodeRepository.save(pincode));
    }

    public PincodeDetailsResponse updatePincode(Long id, PincodeRequest request) {
        GeoPincode pincode = findPincode(id);
        if (request.pincode() != null && !request.pincode().equals(pincode.getPincode())) {
            validatePincode(request.pincode());
            if (pincodeRepository.existsByPincode(request.pincode())) {
                throw new DuplicateResourceException("Pincode already exists.");
            }
            pincode.setPincode(request.pincode());
        }
        if (request.townId() != null && !request.townId().equals(pincode.getTown().getId())) {
            pincode.setTown(findTown(request.townId()));
        }
        applyPincodeFlags(pincode, request);
        return toPincodeDetails(pincode);
    }

    public PincodeDetailsResponse updatePincodeServiceability(Long id, PincodeServiceabilityRequest request) {
        GeoPincode pincode = findPincode(id);
        if (request.serviceable() != null) pincode.setServiceable(request.serviceable());
        if (request.pickupAvailable() != null) pincode.setPickupAvailable(request.pickupAvailable());
        if (request.deliveryAvailable() != null) pincode.setDeliveryAvailable(request.deliveryAvailable());
        if (request.codAvailable() != null) pincode.setCodAvailable(request.codAvailable());
        if (request.prepaidAvailable() != null) pincode.setPrepaidAvailable(request.prepaidAvailable());
        if (request.reversePickupAvailable() != null) pincode.setReversePickupAvailable(request.reversePickupAvailable());
        return toPincodeDetails(pincode);
    }

    @Transactional(readOnly = true)
    public PincodeDetailsResponse getPincodeDetails(String pincodeValue) {
        return toPincodeDetails(pincodeRepository.findByPincode(pincodeValue)
                .orElseThrow(() -> new ResourceNotFoundException("Pincode not found.")));
    }

    @Transactional(readOnly = true)
    public ServiceabilityResponse checkServiceability(String pincodeValue) {
        GeoPincode pincode = pincodeRepository.findByPincode(pincodeValue).orElse(null);
        if (pincode == null) {
            return new ServiceabilityResponse(pincodeValue, false, null, null, null, null, null, "Delivery is not available for this pincode.");
        }
        GeoTown town = pincode.getTown();
        GeoTaluk taluk = town.getTaluk();
        GeoDistrict district = taluk.getDistrict();
        GeoZone zone = district.getZone();
        GeoState state = zone.getState();
        boolean serviceable = pincode.isActive()
                && pincode.isServiceable()
                && town.isActive()
                && taluk.isActive()
                && district.isActive()
                && zone.isActive()
                && state.isActive();
        return new ServiceabilityResponse(
                pincode.getPincode(),
                serviceable,
                state.getName(),
                zone.getName(),
                district.getName(),
                taluk.getName(),
                town.getName(),
                serviceable ? "Delivery is available." : "Delivery is currently unavailable."
        );
    }

    @Transactional(readOnly = true)
    public List<GeographySearchResponse> search(String query) {
        String q = required(query, "Search query is required.");
        List<GeographySearchResponse> results = new ArrayList<>();
        pincodeRepository.findTop10ByPincodeContaining(q).forEach(p -> results.add(new GeographySearchResponse("PINCODE", p.getId(), p.getPincode(), hierarchyPath(p), p.isServiceable())));
        townRepository.findTop10ByNameContainingIgnoreCase(q).forEach(t -> results.add(new GeographySearchResponse("TOWN", t.getId(), t.getName(), hierarchyPath(t), null)));
        talukRepository.findTop10ByNameContainingIgnoreCase(q).forEach(t -> results.add(new GeographySearchResponse("TALUK", t.getId(), t.getName(), hierarchyPath(t), null)));
        districtRepository.findTop10ByNameContainingIgnoreCase(q).forEach(d -> results.add(new GeographySearchResponse("DISTRICT", d.getId(), d.getName(), hierarchyPath(d), null)));
        zoneRepository.findTop10ByNameContainingIgnoreCase(q).forEach(z -> results.add(new GeographySearchResponse("ZONE", z.getId(), z.getName(), hierarchyPath(z), null)));
        stateRepository.findTop10ByNameContainingIgnoreCase(q).forEach(s -> results.add(new GeographySearchResponse("STATE", s.getId(), s.getName(), s.getName(), null)));
        return results.stream().limit(25).toList();
    }

    private void ensureNoDependencies(String type, Map<String, Long> dependencies) {
        boolean inUse = dependencies.values().stream().anyMatch(count -> count > 0);
        if (inUse) {
            throw new GeographyInUseException("This " + type + " cannot be deleted because dependent records exist.", dependencies);
        }
    }
    private void applyPincodeFlags(GeoPincode pincode, PincodeRequest request) {
        if (request.serviceable() != null) pincode.setServiceable(request.serviceable());
        if (request.pickupAvailable() != null) pincode.setPickupAvailable(request.pickupAvailable());
        if (request.deliveryAvailable() != null) pincode.setDeliveryAvailable(request.deliveryAvailable());
        if (request.codAvailable() != null) pincode.setCodAvailable(request.codAvailable());
        if (request.prepaidAvailable() != null) pincode.setPrepaidAvailable(request.prepaidAvailable());
        if (request.reversePickupAvailable() != null) pincode.setReversePickupAvailable(request.reversePickupAvailable());
        if (request.active() != null) pincode.setActive(request.active());
    }

    private StateResponse toStateResponse(GeoState state) {
        long zones = zoneRepository.countByStateId(state.getId());
        long districts = zoneRepository.findByStateIdOrderByNameAsc(state.getId()).stream().mapToLong(z -> districtRepository.countByZoneId(z.getId())).sum();
        long towns = zoneRepository.findByStateIdOrderByNameAsc(state.getId()).stream()
                .flatMap(z -> districtRepository.findByZoneIdOrderByNameAsc(z.getId()).stream())
                .flatMap(d -> talukRepository.findByDistrictIdOrderByNameAsc(d.getId()).stream())
                .mapToLong(t -> townRepository.countByTalukId(t.getId())).sum();
        long pincodes = zoneRepository.findByStateIdOrderByNameAsc(state.getId()).stream()
                .flatMap(z -> districtRepository.findByZoneIdOrderByNameAsc(z.getId()).stream())
                .flatMap(d -> talukRepository.findByDistrictIdOrderByNameAsc(d.getId()).stream())
                .flatMap(t -> townRepository.findByTalukIdOrderByNameAsc(t.getId()).stream())
                .mapToLong(t -> pincodeRepository.countByTownId(t.getId())).sum();
        return new StateResponse(state.getId(), state.getName(), state.getCode(), state.isActive(), zones, districts, towns, pincodes, 0);
    }

    private PincodeSummaryResponse toPincodeSummary(GeoPincode pincode) {
        return new PincodeSummaryResponse(pincode.getId(), pincode.getPincode(), pincode.isServiceable(), pincode.isPickupAvailable(), pincode.isDeliveryAvailable(), pincode.isCodAvailable(), pincode.isPrepaidAvailable(), pincode.isReversePickupAvailable(), pincode.isActive());
    }

    private PincodeDetailsResponse toPincodeDetails(GeoPincode pincode) {
        GeoTown town = pincode.getTown();
        GeoTaluk taluk = town.getTaluk();
        GeoDistrict district = taluk.getDistrict();
        GeoZone zone = district.getZone();
        GeoState state = zone.getState();
        return new PincodeDetailsResponse(
                pincode.getId(), pincode.getPincode(), pincode.isServiceable(), pincode.isPickupAvailable(), pincode.isDeliveryAvailable(), pincode.isCodAvailable(), pincode.isPrepaidAvailable(), pincode.isReversePickupAvailable(), pincode.isActive(),
                toItem(town), toItem(taluk), toItem(district), toItem(zone), toItem(state)
        );
    }

    private GeographyItemResponse toItem(GeoState value) {
        return new GeographyItemResponse(value.getId(), value.getName(), value.getCode(), value.isActive());
    }

    private GeographyItemResponse toItem(GeoZone value) {
        return new GeographyItemResponse(value.getId(), value.getName(), value.getCode(), value.isActive());
    }

    private GeographyItemResponse toItem(GeoDistrict value) {
        return new GeographyItemResponse(value.getId(), value.getName(), value.getCode(), value.isActive());
    }

    private GeographyItemResponse toItem(GeoTaluk value) {
        return new GeographyItemResponse(value.getId(), value.getName(), null, value.isActive());
    }

    private GeographyItemResponse toItem(GeoTown value) {
        return new GeographyItemResponse(value.getId(), value.getName(), null, value.isActive());
    }

    private String hierarchyPath(GeoPincode pincode) {
        return hierarchyPath(pincode.getTown());
    }

    private String hierarchyPath(GeoTown town) {
        GeoTaluk taluk = town.getTaluk();
        return hierarchyPath(taluk) + " / " + town.getName();
    }

    private String hierarchyPath(GeoTaluk taluk) {
        GeoDistrict district = taluk.getDistrict();
        return hierarchyPath(district) + " / " + taluk.getName();
    }

    private String hierarchyPath(GeoDistrict district) {
        GeoZone zone = district.getZone();
        return hierarchyPath(zone) + " / " + district.getName();
    }

    private String hierarchyPath(GeoZone zone) {
        return zone.getState().getName() + " / " + zone.getName();
    }

    private GeoState findState(Long id) {
        return stateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("State not found."));
    }

    private GeoZone findZone(Long id) {
        return zoneRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zone not found."));
    }

    private GeoDistrict findDistrict(Long id) {
        return districtRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("District not found."));
    }

    private GeoTaluk findTaluk(Long id) {
        return talukRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Taluk not found."));
    }

    private GeoTown findTown(Long id) {
        return townRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Town not found."));
    }

    private GeoPincode findPincode(Long id) {
        return pincodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pincode not found."));
    }

    private Long requiredId(Long value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        return code.trim().toUpperCase();
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }

    private void validatePincode(String pincode) {
        if (pincode == null || !pincode.matches(PINCODE_PATTERN)) {
            throw new IllegalArgumentException("Pincode must be a valid 6 digit Indian pincode.");
        }
    }
}



