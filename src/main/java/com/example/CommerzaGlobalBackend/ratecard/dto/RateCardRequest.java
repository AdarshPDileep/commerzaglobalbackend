package com.example.CommerzaGlobalBackend.ratecard.dto;

import com.example.CommerzaGlobalBackend.ratecard.entity.RateCardType;

import java.util.List;

public record RateCardRequest(String name, RateCardType type, Boolean active, List<RateCardSlabRequest> slabs) {
}
