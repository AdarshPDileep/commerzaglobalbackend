package com.example.CommerzaGlobalBackend.ratecard.dto;

import com.example.CommerzaGlobalBackend.ratecard.entity.RateScope;

import java.math.BigDecimal;

public record RateCardSlabRequest(BigDecimal minWeightKg, BigDecimal maxWeightKg, RateScope scope, BigDecimal rate) {
}
