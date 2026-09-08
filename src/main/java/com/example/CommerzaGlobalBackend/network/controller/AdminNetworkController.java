package com.example.CommerzaGlobalBackend.network.controller;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodePincodeRequest;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodePincodeResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeRequest;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkRouteRequest;
import com.example.CommerzaGlobalBackend.network.dto.NetworkRouteResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkStatsResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkStatusRequest;
import com.example.CommerzaGlobalBackend.network.entity.NodeType;
import com.example.CommerzaGlobalBackend.network.service.NetworkService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/network")
public class AdminNetworkController {

    private final NetworkService networkService;

    public AdminNetworkController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping("/stats")
    public ApiResponse<NetworkStatsResponse> stats() {
        return ApiResponse.success(networkService.getStats());
    }

    @GetMapping("/nodes")
    public ApiResponse<PageResponse<NetworkNodeResponse>> nodes(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) NodeType type,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) String search) {
        return ApiResponse.success(networkService.getNodes(page, size, type, status, search));
    }

    @GetMapping("/nodes/{id}")
    public ApiResponse<NetworkNodeResponse> node(@PathVariable Long id) {
        return ApiResponse.success(networkService.getNode(id));
    }

    @PostMapping("/nodes")
    public ApiResponse<NetworkNodeResponse> createNode(@RequestBody NetworkNodeRequest request) {
        return ApiResponse.success(networkService.createNode(request), "Hub created successfully.");
    }

    @PutMapping("/nodes/{id}")
    public ApiResponse<NetworkNodeResponse> updateNode(@PathVariable Long id, @RequestBody NetworkNodeRequest request) {
        return ApiResponse.success(networkService.updateNode(id, request));
    }

    @PatchMapping("/nodes/{id}/status")
    public ApiResponse<NetworkNodeResponse> updateNodeStatus(@PathVariable Long id, @RequestBody NetworkStatusRequest request) {
        return ApiResponse.success(networkService.updateNodeStatus(id, request));
    }

    @DeleteMapping("/nodes/{id}")
    public ApiResponse<NetworkNodeResponse> deleteNode(@PathVariable Long id) {
        return ApiResponse.success(networkService.deleteNode(id));
    }

    @GetMapping("/nodes/{nodeId}/pincodes")
    public ApiResponse<List<NetworkNodePincodeResponse>> nodePincodes(@PathVariable Long nodeId) {
        return ApiResponse.success(networkService.getNodePincodes(nodeId));
    }

    @PostMapping("/nodes/{nodeId}/pincodes")
    public ApiResponse<List<NetworkNodePincodeResponse>> addNodePincodes(@PathVariable Long nodeId,
                                                                         @RequestBody NetworkNodePincodeRequest request) {
        return ApiResponse.success(networkService.addNodePincodes(nodeId, request));
    }

    @DeleteMapping("/nodes/{nodeId}/pincodes/{pincodeId}")
    public ApiResponse<Void> deleteNodePincode(@PathVariable Long nodeId, @PathVariable Long pincodeId) {
        networkService.deleteNodePincode(nodeId, pincodeId);
        return ApiResponse.success(null);
    }

    @GetMapping("/routes")
    public ApiResponse<PageResponse<NetworkRouteResponse>> routes(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String search) {
        return ApiResponse.success(networkService.getRoutes(page, size, status, search));
    }

    @GetMapping("/routes/{id}")
    public ApiResponse<NetworkRouteResponse> route(@PathVariable Long id) {
        return ApiResponse.success(networkService.getRoute(id));
    }

    @PostMapping("/routes")
    public ApiResponse<NetworkRouteResponse> createRoute(@RequestBody NetworkRouteRequest request) {
        return ApiResponse.success(networkService.createRoute(request));
    }

    @PutMapping("/routes/{id}")
    public ApiResponse<NetworkRouteResponse> updateRoute(@PathVariable Long id, @RequestBody NetworkRouteRequest request) {
        return ApiResponse.success(networkService.updateRoute(id, request));
    }

    @PatchMapping("/routes/{id}/status")
    public ApiResponse<NetworkRouteResponse> updateRouteStatus(@PathVariable Long id, @RequestBody NetworkStatusRequest request) {
        return ApiResponse.success(networkService.updateRouteStatus(id, request));
    }

    @DeleteMapping("/routes/{id}")
    public ApiResponse<NetworkRouteResponse> deleteRoute(@PathVariable Long id) {
        return ApiResponse.success(networkService.deleteRoute(id));
    }
}
