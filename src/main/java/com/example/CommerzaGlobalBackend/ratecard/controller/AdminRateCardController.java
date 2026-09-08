package com.example.CommerzaGlobalBackend.ratecard.controller;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardStatusRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.SurchargeRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.SurchargeResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.SurchargeStatusRequest;
import com.example.CommerzaGlobalBackend.ratecard.entity.RateCardType;
import com.example.CommerzaGlobalBackend.ratecard.service.RateCardService;
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
public class AdminRateCardController {

    private final RateCardService rateCardService;

    public AdminRateCardController(RateCardService rateCardService) {
        this.rateCardService = rateCardService;
    }

    @GetMapping("/api/admin/rate-cards")
    public ApiResponse<PageResponse<RateCardResponse>> rateCards(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 @RequestParam(required = false) RateCardType type,
                                                                 @RequestParam(required = false) Boolean active,
                                                                 @RequestParam(required = false) String search) {
        return ApiResponse.success(rateCardService.getRateCards(page, size, type, active, search));
    }

    @GetMapping("/api/admin/rate-cards/{id}")
    public ApiResponse<RateCardResponse> rateCard(@PathVariable Long id) {
        return ApiResponse.success(rateCardService.getRateCard(id));
    }

    @PostMapping("/api/admin/rate-cards")
    public ApiResponse<RateCardResponse> createRateCard(@RequestBody RateCardRequest request) {
        return ApiResponse.success(rateCardService.createRateCard(request), "Rate card created successfully.");
    }

    @PutMapping("/api/admin/rate-cards/{id}")
    public ApiResponse<RateCardResponse> updateRateCard(@PathVariable Long id, @RequestBody RateCardRequest request) {
        return ApiResponse.success(rateCardService.updateRateCard(id, request));
    }

    @PatchMapping("/api/admin/rate-cards/{id}/status")
    public ApiResponse<RateCardResponse> updateRateCardStatus(@PathVariable Long id, @RequestBody RateCardStatusRequest request) {
        return ApiResponse.success(rateCardService.updateRateCardStatus(id, request));
    }

    @DeleteMapping("/api/admin/rate-cards/{id}")
    public ApiResponse<RateCardResponse> deleteRateCard(@PathVariable Long id) {
        return ApiResponse.success(rateCardService.deleteRateCard(id));
    }

    @GetMapping("/api/admin/surcharges")
    public ApiResponse<List<SurchargeResponse>> surcharges() {
        return ApiResponse.success(rateCardService.getSurcharges());
    }

    @PostMapping("/api/admin/surcharges")
    public ApiResponse<SurchargeResponse> createSurcharge(@RequestBody SurchargeRequest request) {
        return ApiResponse.success(rateCardService.createSurcharge(request), "Surcharge created successfully.");
    }

    @PutMapping("/api/admin/surcharges/{id}")
    public ApiResponse<SurchargeResponse> updateSurcharge(@PathVariable Long id, @RequestBody SurchargeRequest request) {
        return ApiResponse.success(rateCardService.updateSurcharge(id, request));
    }

    @PatchMapping("/api/admin/surcharges/{id}/status")
    public ApiResponse<SurchargeResponse> updateSurchargeStatus(@PathVariable Long id, @RequestBody SurchargeStatusRequest request) {
        return ApiResponse.success(rateCardService.updateSurchargeStatus(id, request));
    }

    @DeleteMapping("/api/admin/surcharges/{id}")
    public ApiResponse<SurchargeResponse> deleteSurcharge(@PathVariable Long id) {
        return ApiResponse.success(rateCardService.deleteSurcharge(id));
    }
}
