package com.example.CommerzaGlobalBackend.ratecard.dto;

import com.example.CommerzaGlobalBackend.ratecard.entity.SurchargeType;

import java.math.BigDecimal;

public record SurchargeRequest(String name, SurchargeType type, BigDecimal value, Boolean enabled) {
}
