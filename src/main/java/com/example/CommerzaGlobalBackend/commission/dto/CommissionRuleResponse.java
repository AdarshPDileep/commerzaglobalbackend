package com.example.CommerzaGlobalBackend.commission.dto;

import com.example.CommerzaGlobalBackend.commission.entity.CommissionCondition;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionType;
import com.example.CommerzaGlobalBackend.commission.entity.FranchiseLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CommissionRuleResponse(
        Long id,
        String code,
        FranchiseLevel franchiseLevel,
        CommissionType commissionType,
        BigDecimal value,
        CommissionCondition conditionType,
        Integer minimumVolume,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
