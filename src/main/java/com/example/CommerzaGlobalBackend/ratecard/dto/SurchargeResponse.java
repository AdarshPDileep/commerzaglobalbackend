package com.example.CommerzaGlobalBackend.ratecard.dto;

import com.example.CommerzaGlobalBackend.ratecard.entity.SurchargeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SurchargeResponse(Long id, String name, SurchargeType type, BigDecimal value, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
