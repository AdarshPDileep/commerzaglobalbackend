package com.example.CommerzaGlobalBackend.franchise.dto;

import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseType;

public record FranchiseRequest(
        String name,
        FranchiseType type,
        String ownerName,
        String phone,
        String email,
        String address,
        Long stateId,
        Long zoneId,
        Long districtId,
        Long talukId,
        Long townId,
        Long pincodeId,
        Long networkNodeId
) {
}
