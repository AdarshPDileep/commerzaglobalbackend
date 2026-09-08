package com.example.CommerzaGlobalBackend.franchise.dto;

import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseStatus;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseType;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyItemResponse;
import com.example.CommerzaGlobalBackend.geography.dto.PincodeSummaryResponse;
import com.example.CommerzaGlobalBackend.network.dto.NetworkNodeSummaryResponse;

import java.time.LocalDateTime;

public record FranchiseResponse(
        Long id,
        String code,
        String name,
        FranchiseType type,
        String ownerName,
        String phone,
        String email,
        String address,
        GeographyItemResponse state,
        GeographyItemResponse zone,
        GeographyItemResponse district,
        GeographyItemResponse taluk,
        GeographyItemResponse town,
        PincodeSummaryResponse pincode,
        NetworkNodeSummaryResponse networkNode,
        FranchiseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
