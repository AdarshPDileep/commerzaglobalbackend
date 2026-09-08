package com.example.CommerzaGlobalBackend.network.dto;

import com.example.CommerzaGlobalBackend.network.entity.NodeType;

public record NetworkNodeRequest(
        String name,
        NodeType type,
        Integer dailyCapacity,
        Long managerUserId,
        String address,
        String contactName,
        String contactPhone,
        Long stateId,
        Long zoneId,
        Long districtId,
        Long talukId,
        Long townId,
        Long pincodeId,
        Boolean active
) {
}
