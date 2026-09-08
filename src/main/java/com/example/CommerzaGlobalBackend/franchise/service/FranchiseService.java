package com.example.CommerzaGlobalBackend.franchise.service;

import com.example.CommerzaGlobalBackend.common.exception.ResourceNotFoundException;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseRequest;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseResponse;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseStatusRequest;
import com.example.CommerzaGlobalBackend.franchise.entity.Franchise;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseStatus;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseType;
import com.example.CommerzaGlobalBackend.franchise.exception.FranchiseInUseException;
import com.example.CommerzaGlobalBackend.franchise.repository.FranchiseRepository;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyItemResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeSummaryResponse;
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
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeSummaryResponse;
import com.example.CommerzaGlobalBackend.network.entity.NetworkNode;
import com.example.CommerzaGlobalBackend.network.entity.NodeType;
import com.example.CommerzaGlobalBackend.network.repository.NetworkNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class FranchiseService {

    private static final Set<NodeType> ASSIGNABLE_NODE_TYPES = Set.of(NodeType.HUB, NodeType.BRANCH, NodeType.DELIVERY_CENTER);

    private final FranchiseRepository franchiseRepository;
    private final NetworkNodeRepository nodeRepository;
    private final GeoStateRepository stateRepository;
    private final GeoZoneRepository zoneRepository;
    private final GeoDistrictRepository districtRepository;
    private final GeoTalukRepository talukRepository;
    private final GeoTownRepository townRepository;
    private final GeoPincodeRepository pincodeRepository;

    public FranchiseService(FranchiseRepository franchiseRepository, NetworkNodeRepository nodeRepository, GeoStateRepository stateRepository, GeoZoneRepository zoneRepository, GeoDistrictRepository districtRepository, GeoTalukRepository talukRepository, GeoTownRepository townRepository, GeoPincodeRepository pincodeRepository) {
        this.franchiseRepository = franchiseRepository;
        this.nodeRepository = nodeRepository;
        this.stateRepository = stateRepository;
        this.zoneRepository = zoneRepository;
        this.districtRepository = districtRepository;
        this.talukRepository = talukRepository;
        this.townRepository = townRepository;
        this.pincodeRepository = pincodeRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<FranchiseResponse> getFranchises(int page, int size, FranchiseStatus status, FranchiseType type, String search) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Franchise> result = franchiseRepository.search(type, status, cleanSearch(search), pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public FranchiseResponse getFranchise(Long id) {
        return toResponse(findFranchise(id));
    }

    public FranchiseResponse createFranchise(FranchiseRequest request) {
        FranchiseLocation location = resolveLocation(request);
        Franchise franchise = Franchise.builder()
                .code(generateFranchiseCode())
                .name(required(request.name(), "Franchise name is required."))
                .type(required(request.type(), "Franchise type is required."))
                .ownerName(clean(request.ownerName()))
                .phone(clean(request.phone()))
                .email(clean(request.email()))
                .address(clean(request.address()))
                .state(location.state())
                .zone(location.zone())
                .district(location.district())
                .taluk(location.taluk())
                .town(location.town())
                .pincode(location.pincode())
                .networkNode(resolveNetworkNode(requiredId(request.networkNodeId(), "Assigned hub/branch is required.")))
                .status(FranchiseStatus.ACTIVE)
                .build();
        return toResponse(franchiseRepository.save(franchise));
    }

    public FranchiseResponse updateFranchise(Long id, FranchiseRequest request) {
        Franchise franchise = findFranchise(id);
        FranchiseLocation location = resolveLocation(request);
        franchise.setName(required(request.name(), "Franchise name is required."));
        franchise.setType(required(request.type(), "Franchise type is required."));
        franchise.setOwnerName(clean(request.ownerName()));
        franchise.setPhone(clean(request.phone()));
        franchise.setEmail(clean(request.email()));
        franchise.setAddress(clean(request.address()));
        franchise.setState(location.state());
        franchise.setZone(location.zone());
        franchise.setDistrict(location.district());
        franchise.setTaluk(location.taluk());
        franchise.setTown(location.town());
        franchise.setPincode(location.pincode());
        franchise.setNetworkNode(resolveNetworkNode(requiredId(request.networkNodeId(), "Assigned hub/branch is required.")));
        return toResponse(franchise);
    }

    public FranchiseResponse updateStatus(Long id, FranchiseStatusRequest request) {
        Franchise franchise = findFranchise(id);
        franchise.setStatus(required(request.status(), "Franchise status is required."));
        return toResponse(franchise);
    }

    public FranchiseResponse deleteFranchise(Long id) {
        Franchise franchise = findFranchise(id);
        Map<String, Long> dependencies = new LinkedHashMap<>();
        dependencies.put("shipments", 0L);
        dependencies.put("agents", 0L);
        dependencies.put("codTransactions", 0L);
        dependencies.put("settlements", 0L);
        if (dependencies.values().stream().anyMatch(count -> count > 0)) {
            throw new FranchiseInUseException("FRANCHISE_IN_USE", "This franchise cannot be deleted because it is currently in use.", dependencies);
        }
        FranchiseResponse response = toResponse(franchise);
        franchiseRepository.delete(franchise);
        return response;
    }

    private FranchiseLocation resolveLocation(FranchiseRequest request) {
        GeoState state = findState(requiredId(request.stateId(), "State is required."));
        GeoZone zone = findZone(requiredId(request.zoneId(), "Zone is required."));
        GeoDistrict district = findDistrict(requiredId(request.districtId(), "District is required."));
        GeoTaluk taluk = request.talukId() == null ? null : findTaluk(request.talukId());
        GeoTown town = request.townId() == null ? null : findTown(request.townId());
        GeoPincode pincode = request.pincodeId() == null ? null : findPincode(request.pincodeId());

        if (!zone.getState().getId().equals(state.getId())) throw new IllegalArgumentException("Zone does not belong to selected state.");
        if (!district.getZone().getId().equals(zone.getId())) throw new IllegalArgumentException("District does not belong to selected zone.");
        if (taluk != null && !taluk.getDistrict().getId().equals(district.getId())) throw new IllegalArgumentException("Taluk does not belong to selected district.");
        if (town != null) {
            if (taluk == null) throw new IllegalArgumentException("Taluk is required when town is selected.");
            if (!town.getTaluk().getId().equals(taluk.getId())) throw new IllegalArgumentException("Town does not belong to selected taluk.");
        }
        if (pincode != null) {
            if (town == null) throw new IllegalArgumentException("Town is required when pincode is selected.");
            if (!pincode.getTown().getId().equals(town.getId())) throw new IllegalArgumentException("Pincode does not belong to selected town.");
        }
        return new FranchiseLocation(state, zone, district, taluk, town, pincode);
    }

    private NetworkNode resolveNetworkNode(Long id) {
        NetworkNode node = nodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Assigned hub/branch not found."));
        if (!node.isActive()) throw new IllegalArgumentException("Assigned hub/branch must be active.");
        if (!ASSIGNABLE_NODE_TYPES.contains(node.getType())) throw new IllegalArgumentException("Assigned hub/branch must be a hub, branch, or delivery center.");
        return node;
    }

    private String generateFranchiseCode() {
        long next = Math.max(franchiseRepository.count() + 1, 1);
        String code;
        do {
            code = "FRN" + String.format("%04d", next++);
        } while (franchiseRepository.existsByCode(code));
        return code;
    }

    private FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(
                franchise.getId(), franchise.getCode(), franchise.getName(), franchise.getType(), franchise.getOwnerName(), franchise.getPhone(), franchise.getEmail(), franchise.getAddress(),
                toItem(franchise.getState()), toItem(franchise.getZone()), toItem(franchise.getDistrict()), franchise.getTaluk() == null ? null : toItem(franchise.getTaluk()), franchise.getTown() == null ? null : toItem(franchise.getTown()), franchise.getPincode() == null ? null : toPincodeSummary(franchise.getPincode()),
                new NetworkNodeSummaryResponse(franchise.getNetworkNode().getId(), franchise.getNetworkNode().getCode(), franchise.getNetworkNode().getName(), franchise.getNetworkNode().getType(), franchise.getNetworkNode().isActive()),
                franchise.getStatus(), franchise.getCreatedAt(), franchise.getUpdatedAt()
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

    private PincodeSummaryResponse toPincodeSummary(GeoPincode pincode) {
        return new PincodeSummaryResponse(pincode.getId(), pincode.getPincode(), pincode.isServiceable(), pincode.isPickupAvailable(), pincode.isDeliveryAvailable(), pincode.isCodAvailable(), pincode.isPrepaidAvailable(), pincode.isReversePickupAvailable(), pincode.isActive());
    }

    private Franchise findFranchise(Long id) {
        return franchiseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Franchise not found."));
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

    private String cleanSearch(String value) {
        String clean = clean(value);
        return clean == null ? null : "%" + clean.toLowerCase() + "%";
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private <T> T required(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private Long requiredId(Long value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private record FranchiseLocation(GeoState state, GeoZone zone, GeoDistrict district, GeoTaluk taluk, GeoTown town, GeoPincode pincode) {
    }
}
