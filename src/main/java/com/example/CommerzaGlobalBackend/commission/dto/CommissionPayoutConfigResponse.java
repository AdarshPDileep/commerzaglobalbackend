package com.example.CommerzaGlobalBackend.commission.dto;

import com.example.CommerzaGlobalBackend.commission.entity.BankTransferMode;
import com.example.CommerzaGlobalBackend.commission.entity.PayoutCutoffDay;
import com.example.CommerzaGlobalBackend.commission.entity.PayoutFrequency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommissionPayoutConfigResponse(
        Long id,
        PayoutFrequency frequency,
        PayoutCutoffDay cutoffDay,
        Integer processingDays,
        BigDecimal minimumPayout,
        BankTransferMode bankTransferMode,
        boolean active,
        LocalDateTime updatedAt
) {
}
