package com.example.CommerzaGlobalBackend.network.dto;

import com.example.CommerzaGlobalBackend.network.entity.ServiceType;

public record NetworkNodePincodeResponse(Long id, Long pincodeId, String pincode, ServiceType serviceType, boolean active) {
}
