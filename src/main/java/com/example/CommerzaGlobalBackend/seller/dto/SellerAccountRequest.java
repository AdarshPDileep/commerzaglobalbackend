package com.example.CommerzaGlobalBackend.seller.dto;

public record SellerAccountRequest(String fullName, String email, String mobile, String password, String confirmPassword, Boolean termsAccepted) {
}
