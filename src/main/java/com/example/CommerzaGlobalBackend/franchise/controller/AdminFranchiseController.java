package com.example.CommerzaGlobalBackend.franchise.controller;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseRequest;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseResponse;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseStatusRequest;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseStatus;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseType;
import com.example.CommerzaGlobalBackend.franchise.service.FranchiseService;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
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

@RestController
@RequestMapping("/api/admin/franchises")
public class AdminFranchiseController {

    private final FranchiseService franchiseService;

    public AdminFranchiseController(FranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    @GetMapping
    public ApiResponse<PageResponse<FranchiseResponse>> franchises(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(required = false) String search,
                                                                   @RequestParam(required = false) FranchiseStatus status,
                                                                   @RequestParam(required = false) FranchiseType type) {
        return ApiResponse.success(franchiseService.getFranchises(page, size, status, type, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<FranchiseResponse> franchise(@PathVariable Long id) {
        return ApiResponse.success(franchiseService.getFranchise(id));
    }

    @PostMapping
    public ApiResponse<FranchiseResponse> createFranchise(@RequestBody FranchiseRequest request) {
        return ApiResponse.success(franchiseService.createFranchise(request), "Franchise created successfully.");
    }

    @PutMapping("/{id}")
    public ApiResponse<FranchiseResponse> updateFranchise(@PathVariable Long id, @RequestBody FranchiseRequest request) {
        return ApiResponse.success(franchiseService.updateFranchise(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<FranchiseResponse> updateStatus(@PathVariable Long id, @RequestBody FranchiseStatusRequest request) {
        return ApiResponse.success(franchiseService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<FranchiseResponse> deleteFranchise(@PathVariable Long id) {
        return ApiResponse.success(franchiseService.deleteFranchise(id));
    }
}
