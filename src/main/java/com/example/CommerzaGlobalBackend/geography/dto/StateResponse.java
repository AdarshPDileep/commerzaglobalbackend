package com.example.CommerzaGlobalBackend.geography.dto;

public record StateResponse(Long id, String name, String code, boolean active, long zones, long districts, long towns, long pincodes, long serviceablePincodes) {
}
