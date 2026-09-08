package com.example.CommerzaGlobalBackend.geography.dto;

public record ServiceabilityResponse(String pincode, boolean serviceable, String state, String zone, String district, String taluk, String town, String message) {
}
