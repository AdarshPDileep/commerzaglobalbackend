package com.example.CommerzaGlobalBackend.geography.controller;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.geography.dto.DistrictRequest;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyItemResponse;
import com.example.CommerzaGlobalBackend.geography.dto.GeographySearchResponse;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyStatsResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeDetailsResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeRequest;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeServiceabilityRequest;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeSummaryResponse;
import com.example.CommerzaGlobalBackend.geography.dto.StateRequest;
import com.example.CommerzaGlobalBackend.geography.dto.StateResponse;
import com.example.CommerzaGlobalBackend.geography.dto.TalukRequest;
import com.example.CommerzaGlobalBackend.geography.dto.TownRequest;
import com.example.CommerzaGlobalBackend.geography.dto.ZoneRequest;
import com.example.CommerzaGlobalBackend.geography.service.GeographyService;
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
@RequestMapping("/api/admin/geography")
public class AdminGeographyController {

    private final GeographyService geographyService;

    public AdminGeographyController(GeographyService geographyService) {
        this.geographyService = geographyService;
    }

    @GetMapping("/stats")
    public ApiResponse<GeographyStatsResponse> stats() {
        return ApiResponse.success(geographyService.getStats());
    }

    @GetMapping("/states")
    public ApiResponse<List<StateResponse>> states() {
        return ApiResponse.success(geographyService.getStates());
    }

    @PostMapping("/states")
    public ApiResponse<GeographyItemResponse> createState(@RequestBody StateRequest request) {
        return ApiResponse.success(geographyService.createState(request));
    }

    @PutMapping("/states/{id}")
    public ApiResponse<GeographyItemResponse> updateState(@PathVariable Long id, @RequestBody StateRequest request) {
        return ApiResponse.success(geographyService.updateState(id, request));
    }

    @GetMapping("/states/{stateId}/zones")
    public ApiResponse<List<GeographyItemResponse>> zones(@PathVariable Long stateId) {
        return ApiResponse.success(geographyService.getZonesByState(stateId));
    }

    @PostMapping("/zones")
    public ApiResponse<GeographyItemResponse> createZone(@RequestBody ZoneRequest request) {
        return ApiResponse.success(geographyService.createZone(request));
    }

    @PutMapping("/zones/{id}")
    public ApiResponse<GeographyItemResponse> updateZone(@PathVariable Long id, @RequestBody ZoneRequest request) {
        return ApiResponse.success(geographyService.updateZone(id, request));
    }

    @GetMapping("/zones/{zoneId}/districts")
    public ApiResponse<List<GeographyItemResponse>> districts(@PathVariable Long zoneId) {
        return ApiResponse.success(geographyService.getDistrictsByZone(zoneId));
    }

    @PostMapping("/districts")
    public ApiResponse<GeographyItemResponse> createDistrict(@RequestBody DistrictRequest request) {
        return ApiResponse.success(geographyService.createDistrict(request));
    }

    @PutMapping("/districts/{id}")
    public ApiResponse<GeographyItemResponse> updateDistrict(@PathVariable Long id, @RequestBody DistrictRequest request) {
        return ApiResponse.success(geographyService.updateDistrict(id, request));
    }

    @GetMapping("/districts/{districtId}/taluks")
    public ApiResponse<List<GeographyItemResponse>> taluks(@PathVariable Long districtId) {
        return ApiResponse.success(geographyService.getTaluksByDistrict(districtId));
    }

    @PostMapping("/taluks")
    public ApiResponse<GeographyItemResponse> createTaluk(@RequestBody TalukRequest request) {
        return ApiResponse.success(geographyService.createTaluk(request));
    }

    @PutMapping("/taluks/{id}")
    public ApiResponse<GeographyItemResponse> updateTaluk(@PathVariable Long id, @RequestBody TalukRequest request) {
        return ApiResponse.success(geographyService.updateTaluk(id, request));
    }

    @GetMapping("/taluks/{talukId}/towns")
    public ApiResponse<List<GeographyItemResponse>> towns(@PathVariable Long talukId) {
        return ApiResponse.success(geographyService.getTownsByTaluk(talukId));
    }

    @PostMapping("/towns")
    public ApiResponse<GeographyItemResponse> createTown(@RequestBody TownRequest request) {
        return ApiResponse.success(geographyService.createTown(request));
    }

    @PutMapping("/towns/{id}")
    public ApiResponse<GeographyItemResponse> updateTown(@PathVariable Long id, @RequestBody TownRequest request) {
        return ApiResponse.success(geographyService.updateTown(id, request));
    }

    @GetMapping("/towns/{townId}/pincodes")
    public ApiResponse<PageResponse<PincodeSummaryResponse>> pincodes(@PathVariable Long townId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(geographyService.getPincodesByTown(townId, page, size));
    }

    @PostMapping("/pincodes")
    public ApiResponse<PincodeDetailsResponse> createPincode(@RequestBody PincodeRequest request) {
        return ApiResponse.success(geographyService.createPincode(request));
    }

    @PutMapping("/pincodes/{id}")
    public ApiResponse<PincodeDetailsResponse> updatePincode(@PathVariable Long id, @RequestBody PincodeRequest request) {
        return ApiResponse.success(geographyService.updatePincode(id, request));
    }

    @PatchMapping("/pincodes/{id}/serviceability")
    public ApiResponse<PincodeDetailsResponse> updatePincodeServiceability(@PathVariable Long id, @RequestBody PincodeServiceabilityRequest request) {
        return ApiResponse.success(geographyService.updatePincodeServiceability(id, request));
    }

    @GetMapping("/pincodes/{pincode}")
    public ApiResponse<PincodeDetailsResponse> pincodeDetails(@PathVariable String pincode) {
        return ApiResponse.success(geographyService.getPincodeDetails(pincode));
    }

    @GetMapping("/search")
    public ApiResponse<List<GeographySearchResponse>> search(@RequestParam("q") String query) {
        return ApiResponse.success(geographyService.search(query));
    }
}
