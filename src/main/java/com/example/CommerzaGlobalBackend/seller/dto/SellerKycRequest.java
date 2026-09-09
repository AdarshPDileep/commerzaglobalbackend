package com.example.CommerzaGlobalBackend.seller.dto;

import com.example.CommerzaGlobalBackend.seller.entity.IdProofType;

public record SellerKycRequest(IdProofType idProofType, String idProofNumber) {
}
