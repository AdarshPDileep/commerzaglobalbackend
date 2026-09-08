package com.example.CommerzaGlobalBackend.network.dto;

public record NetworkStatsResponse(long totalHubs, long totalBranches, long activeRoutes, long totalCapacity) {
}
