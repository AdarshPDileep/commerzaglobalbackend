package com.example.CommerzaGlobalBackend.seller.dto;

public record PickupServiceabilityResponse(boolean serviceable, boolean pickupAvailable, String pincode, String state, String district, String town, String message) {
}
