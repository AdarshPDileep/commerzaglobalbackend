package com.example.CommerzaGlobalBackend.commission.dto;

import com.example.CommerzaGlobalBackend.commission.entity.BankTransferMode;
import com.example.CommerzaGlobalBackend.commission.entity.PayoutCutoffDay;
import com.example.CommerzaGlobalBackend.commission.entity.PayoutFrequency;

import java.math.BigDecimal;

public record CommissionPayoutConfigRequest(
        PayoutFrequency frequency,
        PayoutCutoffDay cutoffDay,
        Integer processingDays,
        BigDecimal minimumPayout,
        BankTransferMode bankTransferMode,
        Boolean active
) {
}
