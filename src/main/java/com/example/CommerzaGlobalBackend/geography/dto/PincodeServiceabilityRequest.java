package com.example.CommerzaGlobalBackend.geography.dto;

public record PincodeServiceabilityRequest(Boolean serviceable, Boolean pickupAvailable, Boolean deliveryAvailable, Boolean codAvailable, Boolean prepaidAvailable, Boolean reversePickupAvailable) {
}
