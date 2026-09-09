package com.example.CommerzaGlobalBackend.seller.dto;

public record SellerPickupAddressRequest(String locationName, String contactPerson, String contactPhone, String address, String pincode) {
}
