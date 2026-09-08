package com.example.CommerzaGlobalBackend.geography.dto;

public record PincodeDetailsResponse(Long id, String pincode, boolean serviceable, boolean pickupAvailable, boolean deliveryAvailable, boolean codAvailable, boolean prepaidAvailable, boolean reversePickupAvailable, boolean active, GeographyItemResponse town, GeographyItemResponse taluk, GeographyItemResponse district, GeographyItemResponse zone, GeographyItemResponse state) {
}
