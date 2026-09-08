package com.example.CommerzaGlobalBackend.network.dto;

import com.example.CommerzaGlobalBackend.network.entity.ServiceType;

import java.util.List;

public record NetworkNodePincodeRequest(List<Long> pincodeIds, ServiceType serviceType) {
}
