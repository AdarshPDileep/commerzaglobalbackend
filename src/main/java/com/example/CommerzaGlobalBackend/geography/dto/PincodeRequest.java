package com.example.CommerzaGlobalBackend.geography.dto;

public record PincodeRequest(String pincode, Long townId, Boolean serviceable, Boolean pickupAvailable, Boolean deliveryAvailable, Boolean codAvailable, Boolean prepaidAvailable, Boolean reversePickupAvailable, Boolean active) {
}
