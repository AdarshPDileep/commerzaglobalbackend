package com.example.CommerzaGlobalBackend.seller.dto;

import com.example.CommerzaGlobalBackend.seller.entity.BusinessType;

public record SellerBusinessRequest(String businessName, BusinessType businessType, Boolean gstRegistered, String gstin, String businessPan, String websiteUrl) {
}
