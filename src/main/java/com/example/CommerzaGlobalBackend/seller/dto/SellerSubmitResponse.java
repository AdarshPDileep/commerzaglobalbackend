package com.example.CommerzaGlobalBackend.seller.dto;

import com.example.CommerzaGlobalBackend.seller.entity.SellerAccountStatus;
import com.example.CommerzaGlobalBackend.seller.entity.SellerApplicationStatus;

public record SellerSubmitResponse(String sellerCode, SellerApplicationStatus applicationStatus, SellerAccountStatus accountStatus) {
}
