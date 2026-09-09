package com.example.CommerzaGlobalBackend.seller.dto;

import com.example.CommerzaGlobalBackend.seller.entity.SellerAccountStatus;
import com.example.CommerzaGlobalBackend.seller.entity.SellerApplicationStatus;

public record SellerRegistrationResponse(
        Long id,
        String sellerCode,
        String fullName,
        String email,
        String mobile,
        boolean mobileVerified,
        boolean emailVerified,
        SellerApplicationStatus applicationStatus,
        SellerAccountStatus accountStatus,
        boolean accountComplete,
        boolean businessComplete,
        boolean kycComplete,
        boolean bankComplete,
        boolean pickupComplete,
        SellerPickupAddressResponse pickupAddress
) {
}
