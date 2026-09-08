package com.example.CommerzaGlobalBackend.network.dto;

public record NetworkRouteResponse(
        Long id,
        String routeCode,
        NetworkNodeSummaryResponse origin,
        NetworkNodeSummaryResponse destination,
        Double distanceKm,
        Integer transitHours,
        Integer dailyCapacity,
        boolean active
) {
}
