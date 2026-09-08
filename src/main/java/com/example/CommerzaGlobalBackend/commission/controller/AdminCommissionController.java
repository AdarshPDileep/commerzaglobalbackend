package com.example.CommerzaGlobalBackend.commission.controller;

import com.example.CommerzaGlobalBackend.commission.dto.CommissionPayoutConfigRequest;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionPayoutConfigResponse;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionRuleRequest;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionRuleResponse;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionRuleStatusRequest;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionType;
import com.example.CommerzaGlobalBackend.commission.entity.FranchiseLevel;
import com.example.CommerzaGlobalBackend.commission.service.CommissionService;
import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminCommissionController {

    private final CommissionService commissionService;

    public AdminCommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping("/api/admin/commissions/rules")
    public ApiResponse<PageResponse<CommissionRuleResponse>> rules(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size,
                                                                   @RequestParam(required = false) String search,
                                                                   @RequestParam(required = false) String status,
                                                                   @RequestParam(required = false) FranchiseLevel level,
                                                                   @RequestParam(required = false) CommissionType type) {
        return ApiResponse.success(commissionService.getRules(page, size, status, level, type, search));
    }

    @GetMapping("/api/admin/commissions/rules/{id}")
    public ApiResponse<CommissionRuleResponse> rule(@PathVariable Long id) {
        return ApiResponse.success(commissionService.getRule(id));
    }

    @PostMapping("/api/admin/commissions/rules")
    public ApiResponse<CommissionRuleResponse> createRule(@RequestBody CommissionRuleRequest request) {
        return ApiResponse.success(commissionService.createRule(request), "Commission rule created successfully.");
    }

    @PutMapping("/api/admin/commissions/rules/{id}")
    public ApiResponse<CommissionRuleResponse> updateRule(@PathVariable Long id, @RequestBody CommissionRuleRequest request) {
        return ApiResponse.success(commissionService.updateRule(id, request));
    }

    @PatchMapping("/api/admin/commissions/rules/{id}/status")
    public ApiResponse<CommissionRuleResponse> updateRuleStatus(@PathVariable Long id, @RequestBody CommissionRuleStatusRequest request) {
        return ApiResponse.success(commissionService.updateRuleStatus(id, request));
    }

    @DeleteMapping("/api/admin/commissions/rules/{id}")
    public ApiResponse<CommissionRuleResponse> deleteRule(@PathVariable Long id) {
        return ApiResponse.success(commissionService.deleteRule(id));
    }

    @GetMapping("/api/admin/commissions/payout-config")
    public ApiResponse<CommissionPayoutConfigResponse> payoutConfig() {
        return ApiResponse.success(commissionService.getPayoutConfig());
    }

    @PutMapping("/api/admin/commissions/payout-config")
    public ApiResponse<CommissionPayoutConfigResponse> updatePayoutConfig(@RequestBody CommissionPayoutConfigRequest request) {
        return ApiResponse.success(commissionService.updatePayoutConfig(request));
    }
}
