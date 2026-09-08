package com.example.CommerzaGlobalBackend.network.service;

import com.example.CommerzaGlobalBackend.common.exception.DuplicateResourceException;
import com.example.CommerzaGlobalBackend.common.exception.ResourceNotFoundException;
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
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodePincodeRequest;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodePincodeResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeRequest;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeSummaryResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkRouteRequest;
import com.example.CommerzaGlobalBackend.network.dto.NetworkRouteResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkStatsResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkStatusRequest;
import com.example.CommerzaGlobalBackend.network.entity.NetworkNode;
import com.example.CommerzaGlobalBackend.network.entity.NetworkNodePincode;
import com.example.CommerzaGlobalBackend.network.entity.NetworkRoute;
import com.example.CommerzaGlobalBackend.network.entity.NodeType;
import com.example.CommerzaGlobalBackend.network.entity.ServiceType;
import com.example.CommerzaGlobalBackend.network.exception.NetworkInUseException;
import com.example.CommerzaGlobalBackend.network.repository.NetworkNodePincodeRepository;
import com.example.CommerzaGlobalBackend.network.repository.NetworkNodeRepository;
import com.example.CommerzaGlobalBackend.network.repository.NetworkRouteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NetworkService {

    private final NetworkNodeRepository nodeRepository;
    private final NetworkNodePincodeRepository nodePincodeRepository;
    private final NetworkRouteRepository routeRepository;
    private final GeoStateRepository stateRepository;
    private final GeoZoneRepository zoneRepository;
    private final GeoDistrictRepository districtRepository;
    private final GeoTalukRepository talukRepository;
    private final GeoTownRepository townRepository;
    private final GeoPincodeRepository pincodeRepository;

    public NetworkService(NetworkNodeRepository nodeRepository,
                          NetworkNodePincodeRepository nodePincodeRepository,
                          NetworkRouteRepository routeRepository,
                          GeoStateRepository stateRepository,
                          GeoZoneRepository zoneRepository,
                          GeoDistrictRepository districtRepository,
                          GeoTalukRepository talukRepository,
                          GeoTownRepository townRepository,
                          GeoPincodeRepository pincodeRepository) {
        this.nodeRepository = nodeRepository;
        this.nodePincodeRepository = nodePincodeRepository;
        this.routeRepository = routeRepository;
        this.stateRepository = stateRepository;
        this.zoneRepository = zoneRepository;
        this.districtRepository = districtRepository;
        this.talukRepository = talukRepository;
        this.townRepository = townRepository;
        this.pincodeRepository = pincodeRepository;
    }

    @Transactional(readOnly = true)
    public NetworkStatsResponse getStats() {
        return new NetworkStatsResponse(
                nodeRepository.countByType(NodeType.HUB),
                nodeRepository.countByType(NodeType.BRANCH),
                routeRepository.countByActiveTrue(),
                nodeRepository.sumDailyCapacity()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<NetworkNodeResponse> getNodes(int page, int size, NodeType type, String status, String search) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<NetworkNode> result = nodeRepository.search(type, parseStatus(status), cleanSearch(search), pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toNodeResponse).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public NetworkNodeResponse getNode(Long id) {
        return toNodeResponse(findNode(id));
    }

    public NetworkNodeResponse createNode(NetworkNodeRequest request) {
        NodeLocation location = resolveLocation(request);
        NodeType type = required(request.type(), "Node type is required.");
        NetworkNode node = NetworkNode.builder()
                .code(generateNodeCode(type))
                .name(required(request.name(), "Node name is required."))
                .type(type)
                .dailyCapacity(positive(request.dailyCapacity(), "Daily capacity must be greater than zero."))
                .managerUserId(request.managerUserId())
                .address(clean(request.address()))
                .contactName(clean(request.contactName()))
                .contactPhone(clean(request.contactPhone()))
                .state(location.state())
                .zone(location.zone())
                .district(location.district())
                .taluk(location.taluk())
                .town(location.town())
                .pincode(location.pincode())
                .active(defaultTrue(request.active()))
                .build();
        return toNodeResponse(nodeRepository.save(node));
    }

    public NetworkNodeResponse updateNode(Long id, NetworkNodeRequest request) {
        NetworkNode node = findNode(id);
        NodeLocation location = resolveLocation(request);
        node.setName(required(request.name(), "Node name is required."));
        node.setType(required(request.type(), "Node type is required."));
        node.setDailyCapacity(positive(request.dailyCapacity(), "Daily capacity must be greater than zero."));
        node.setManagerUserId(request.managerUserId());
        node.setAddress(clean(request.address()));
        node.setContactName(clean(request.contactName()));
        node.setContactPhone(clean(request.contactPhone()));
        node.setState(location.state());
        node.setZone(location.zone());
        node.setDistrict(location.district());
        node.setTaluk(location.taluk());
        node.setTown(location.town());
        node.setPincode(location.pincode());
        if (request.active() != null) node.setActive(request.active());
        return toNodeResponse(node);
    }

    public NetworkNodeResponse updateNodeStatus(Long id, NetworkStatusRequest request) {
        NetworkNode node = findNode(id);
        if (request.active() == null) throw new IllegalArgumentException("Active status is required.");
        node.setActive(request.active());
        return toNodeResponse(node);
    }

    public NetworkNodeResponse deleteNode(Long id) {
        NetworkNode node = findNode(id);
        Map<String, Long> dependencies = new LinkedHashMap<>();
        dependencies.put("pincodes", nodePincodeRepository.countByNodeId(node.getId()));
        dependencies.put("routes", routeRepository.countByOriginIdOrDestinationId(node.getId(), node.getId()));
        dependencies.put("shipments", 0L);
        dependencies.put("franchises", 0L);
        dependencies.put("rateCards", 0L);
        ensureNoDependencies("NETWORK_NODE_IN_USE", "This hub cannot be deleted because it is currently in use.", dependencies);
        NetworkNodeResponse response = toNodeResponse(node);
        nodeRepository.delete(node);
        return response;
    }

    @Transactional(readOnly = true)
    public List<NetworkNodePincodeResponse> getNodePincodes(Long nodeId) {
        findNode(nodeId);
        return nodePincodeRepository.findByNodeIdOrderByPincodePincodeAsc(nodeId).stream().map(this::toNodePincodeResponse).toList();
    }

    public List<NetworkNodePincodeResponse> addNodePincodes(Long nodeId, NetworkNodePincodeRequest request) {
        NetworkNode node = findNode(nodeId);
        ServiceType serviceType = required(request.serviceType(), "Service type is required.");
        if (request.pincodeIds() == null || request.pincodeIds().isEmpty()) {
            throw new IllegalArgumentException("At least one pincode is required.");
        }
        for (Long pincodeId : request.pincodeIds()) {
            GeoPincode pincode = findPincode(pincodeId);
            NetworkNodePincode mapping = nodePincodeRepository.findByNodeIdAndPincodeId(node.getId(), pincode.getId())
                    .orElse(NetworkNodePincode.builder().node(node).pincode(pincode).build());
            mapping.setServiceType(serviceType);
            mapping.setActive(true);
            nodePincodeRepository.save(mapping);
        }
        return getNodePincodes(nodeId);
    }

    public void deleteNodePincode(Long nodeId, Long pincodeId) {
        findNode(nodeId);
        findPincode(pincodeId);
        if (nodePincodeRepository.findByNodeIdAndPincodeId(nodeId, pincodeId).isEmpty()) {
            throw new ResourceNotFoundException("Service area pincode not found.");
        }
        nodePincodeRepository.deleteByNodeIdAndPincodeId(nodeId, pincodeId);
    }

    @Transactional(readOnly = true)
    public PageResponse<NetworkRouteResponse> getRoutes(int page, int size, String status, String search) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<NetworkRoute> result = routeRepository.search(parseStatus(status), cleanSearch(search), pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toRouteResponse).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public NetworkRouteResponse getRoute(Long id) {
        return toRouteResponse(findRoute(id));
    }

    public NetworkRouteResponse createRoute(NetworkRouteRequest request) {
        NetworkNode origin = findNode(requiredId(request.originNodeId(), "Origin node is required."));
        NetworkNode destination = findNode(requiredId(request.destinationNodeId(), "Destination node is required."));
        validateRouteEndpoints(origin, destination, null);
        NetworkRoute route = NetworkRoute.builder()
                .routeCode(generateRouteCode())
                .origin(origin)
                .destination(destination)
                .distanceKm(nonNegative(request.distanceKm(), "Distance cannot be negative."))
                .transitHours(nonNegative(request.transitHours(), "Transit hours cannot be negative."))
                .dailyCapacity(positive(request.dailyCapacity(), "Daily capacity must be greater than zero."))
                .active(defaultTrue(request.active()))
                .build();
        return toRouteResponse(routeRepository.save(route));
    }

    public NetworkRouteResponse updateRoute(Long id, NetworkRouteRequest request) {
        NetworkRoute route = findRoute(id);
        NetworkNode origin = findNode(requiredId(request.originNodeId(), "Origin node is required."));
        NetworkNode destination = findNode(requiredId(request.destinationNodeId(), "Destination node is required."));
        validateRouteEndpoints(origin, destination, route.getId());
        route.setOrigin(origin);
        route.setDestination(destination);
        route.setDistanceKm(nonNegative(request.distanceKm(), "Distance cannot be negative."));
        route.setTransitHours(nonNegative(request.transitHours(), "Transit hours cannot be negative."));
        route.setDailyCapacity(positive(request.dailyCapacity(), "Daily capacity must be greater than zero."));
        if (request.active() != null) route.setActive(request.active());
        return toRouteResponse(route);
    }

    public NetworkRouteResponse updateRouteStatus(Long id, NetworkStatusRequest request) {
        NetworkRoute route = findRoute(id);
        if (request.active() == null) throw new IllegalArgumentException("Active status is required.");
        route.setActive(request.active());
        return toRouteResponse(route);
    }

    public NetworkRouteResponse deleteRoute(Long id) {
        NetworkRoute route = findRoute(id);
        NetworkRouteResponse response = toRouteResponse(route);
        routeRepository.delete(route);
        return response;
    }

    private NodeLocation resolveLocation(NetworkNodeRequest request) {
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
        return new NodeLocation(state, zone, district, taluk, town, pincode);
    }

    private void validateRouteEndpoints(NetworkNode origin, NetworkNode destination, Long currentRouteId) {
        if (origin.getId().equals(destination.getId())) throw new IllegalArgumentException("Origin and destination nodes must be different.");
        routeRepository.findByOriginIdAndDestinationId(origin.getId(), destination.getId()).ifPresent(existing -> {
            if (currentRouteId == null || !existing.getId().equals(currentRouteId)) {
                throw new DuplicateResourceException("Route already exists for these nodes.");
            }
        });
    }

    private void ensureNoDependencies(String code, String message, Map<String, Long> dependencies) {
        if (dependencies.values().stream().anyMatch(count -> count > 0)) {
            throw new NetworkInUseException(code, message, dependencies);
        }
    }

    private String generateNodeCode(NodeType type) {
        return generateCode(nodePrefix(type), nodeRepository.countByType(type) + 1, nodeRepository::existsByCode);
    }

    private String generateRouteCode() {
        return generateCode("RTE", routeRepository.count() + 1, routeRepository::existsByRouteCode);
    }

    private String generateCode(String prefix, long seed, java.util.function.Predicate<String> exists) {
        long next = Math.max(seed, 1);
        String code;
        do {
            code = prefix + String.format("%04d", next++);
        } while (exists.test(code));
        return code;
    }

    private String nodePrefix(NodeType type) {
        return switch (type) {
            case HUB -> "HUB";
            case BRANCH -> "BRN";
            case SORTING_CENTER -> "SRT";
            case DELIVERY_CENTER -> "DC";
        };
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        if ("ACTIVE".equalsIgnoreCase(status)) return true;
        if ("INACTIVE".equalsIgnoreCase(status) || "DISABLED".equalsIgnoreCase(status)) return false;
        throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE.");
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

    private Integer positive(Integer value, String message) {
        if (value == null || value <= 0) throw new IllegalArgumentException(message);
        return value;
    }

    private Integer nonNegative(Integer value, String message) {
        if (value != null && value < 0) throw new IllegalArgumentException(message);
        return value;
    }

    private Double nonNegative(Double value, String message) {
        if (value != null && value < 0) throw new IllegalArgumentException(message);
        return value;
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }

    private NetworkNode findNode(Long id) {
        return nodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Network node not found."));
    }

    private NetworkRoute findRoute(Long id) {
        return routeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Network route not found."));
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

    private NetworkNodeResponse toNodeResponse(NetworkNode node) {
        return new NetworkNodeResponse(
                node.getId(), node.getCode(), node.getName(), node.getType(), node.getDailyCapacity(),
                node.getManagerUserId(), node.getAddress(), node.getContactName(), node.getContactPhone(), node.isActive(),
                toItem(node.getState()), toItem(node.getZone()), toItem(node.getDistrict()),
                node.getTaluk() == null ? null : toItem(node.getTaluk()),
                node.getTown() == null ? null : toItem(node.getTown()),
                node.getPincode() == null ? null : toPincodeSummary(node.getPincode())
        );
    }

    private NetworkNodeSummaryResponse toNodeSummary(NetworkNode node) {
        return new NetworkNodeSummaryResponse(node.getId(), node.getCode(), node.getName(), node.getType(), node.isActive());
    }

    private NetworkNodePincodeResponse toNodePincodeResponse(NetworkNodePincode mapping) {
        return new NetworkNodePincodeResponse(mapping.getId(), mapping.getPincode().getId(), mapping.getPincode().getPincode(), mapping.getServiceType(), mapping.isActive());
    }

    private NetworkRouteResponse toRouteResponse(NetworkRoute route) {
        return new NetworkRouteResponse(route.getId(), route.getRouteCode(), toNodeSummary(route.getOrigin()), toNodeSummary(route.getDestination()), route.getDistanceKm(), route.getTransitHours(), route.getDailyCapacity(), route.isActive());
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

    private record NodeLocation(GeoState state, GeoZone zone, GeoDistrict district, GeoTaluk taluk, GeoTown town, GeoPincode pincode) {
    }
}

