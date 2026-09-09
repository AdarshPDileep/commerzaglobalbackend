package com.example.CommerzaGlobalBackend.seller.controller;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.seller.dto.PickupServiceabilityResponse;
import com.example.CommerzaGlobalBackend.seller.dto.SellerAccountRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerBankRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerBusinessRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerKycRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerPickupAddressRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerRegistrationResponse;
import com.example.CommerzaGlobalBackend.seller.dto.SellerSubmitResponse;
import com.example.CommerzaGlobalBackend.seller.dto.VerifyOtpRequest;
import com.example.CommerzaGlobalBackend.seller.service.SellerRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/register")
public class SellerRegistrationController {

    private final SellerRegistrationService sellerRegistrationService;

    public SellerRegistrationController(SellerRegistrationService sellerRegistrationService) {
        this.sellerRegistrationService = sellerRegistrationService;
    }

    @GetMapping("/pickup-serviceability/{pincode}")
    public ApiResponse<PickupServiceabilityResponse> pickupServiceability(@PathVariable String pincode) {
        return ApiResponse.success(sellerRegistrationService.checkPickupServiceability(pincode));
    }
    @PostMapping("/account")
    public ApiResponse<SellerRegistrationResponse> registerAccount(@RequestBody SellerAccountRequest request) {
        return ApiResponse.success(sellerRegistrationService.registerAccount(request), "Seller account created successfully.");
    }

    @PostMapping("/verify-otp")
    public ApiResponse<SellerRegistrationResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ApiResponse.success(sellerRegistrationService.verifyOtp(request), "OTP verified successfully.");
    }

    @PostMapping("/resend-otp")
    public ApiResponse<SellerRegistrationResponse> resendOtp(@RequestBody VerifyOtpRequest request) {
        return ApiResponse.success(sellerRegistrationService.resendOtp(request), "OTP resent successfully.");
    }

    @PutMapping("/{sellerId}/business")
    public ApiResponse<SellerRegistrationResponse> business(@PathVariable Long sellerId, @RequestBody SellerBusinessRequest request) {
        return ApiResponse.success(sellerRegistrationService.saveBusiness(sellerId, request));
    }

    @PutMapping("/{sellerId}/kyc")
    public ApiResponse<SellerRegistrationResponse> kyc(@PathVariable Long sellerId, @RequestBody SellerKycRequest request) {
        return ApiResponse.success(sellerRegistrationService.saveKyc(sellerId, request));
    }

    @PutMapping("/{sellerId}/bank")
    public ApiResponse<SellerRegistrationResponse> bank(@PathVariable Long sellerId, @RequestBody SellerBankRequest request) {
        return ApiResponse.success(sellerRegistrationService.saveBank(sellerId, request));
    }

    @PostMapping("/{sellerId}/pickup-address")
    public ApiResponse<SellerRegistrationResponse> pickupAddress(@PathVariable Long sellerId, @RequestBody SellerPickupAddressRequest request) {
        return ApiResponse.success(sellerRegistrationService.savePickupAddress(sellerId, request));
    }

    @PostMapping("/{sellerId}/submit")
    public ApiResponse<SellerSubmitResponse> submit(@PathVariable Long sellerId) {
        return ApiResponse.success(sellerRegistrationService.submit(sellerId), "Seller application submitted successfully.");
    }
}

