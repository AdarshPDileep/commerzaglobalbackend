package com.example.CommerzaGlobalBackend.network.dto;

import com.example.CommerzaGlobalBackend.geography.dto.GeographyItemResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeSummaryResponse;
import com.example.CommerzaGlobalBackend.network.entity.NodeType;

public record NetworkNodeResponse(
        Long id,
        String code,
        String name,
        NodeType type,
        Integer dailyCapacity,
        Long managerUserId,
        String address,
        String contactName,
        String contactPhone,
        boolean active,
        GeographyItemResponse state,
        GeographyItemResponse zone,
        GeographyItemResponse district,
        GeographyItemResponse taluk,
        GeographyItemResponse town,
        PincodeSummaryResponse pincode
) {
}
