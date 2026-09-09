package com.example.CommerzaGlobalBackend.seller.dto;

public record SellerPickupAddressResponse(Long id, String locationName, String contactPerson, String contactPhone, String address, String pincode, String town, String taluk, String district, String zone, String state) {
}
