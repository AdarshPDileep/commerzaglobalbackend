package com.example.CommerzaGlobalBackend.seller.dto;

public record SellerBankRequest(String bankName, String accountHolderName, String accountNumber, String confirmAccountNumber, String ifscCode) {
}
