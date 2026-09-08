package com.example.CommerzaGlobalBackend.network.dto;

public record NetworkRouteRequest(
        Long originNodeId,
        Long destinationNodeId,
        Double distanceKm,
        Integer transitHours,
        Integer dailyCapacity,
        Boolean active
) {
}
