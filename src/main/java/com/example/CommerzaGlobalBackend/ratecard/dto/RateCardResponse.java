package com.example.CommerzaGlobalBackend.ratecard.dto;

import com.example.CommerzaGlobalBackend.ratecard.entity.RateCardType;

import java.time.LocalDateTime;
import java.util.List;

public record RateCardResponse(Long id, String code, String name, RateCardType type, boolean active, String applicableTo, List<RateCardSlabResponse> slabs, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
