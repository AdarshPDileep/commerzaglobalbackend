package com.example.CommerzaGlobalBackend.geography.dto;

public record PincodeSummaryResponse(Long id, String pincode, boolean serviceable, boolean pickupAvailable, boolean deliveryAvailable, boolean codAvailable, boolean prepaidAvailable, boolean reversePickupAvailable, boolean active) {
}
