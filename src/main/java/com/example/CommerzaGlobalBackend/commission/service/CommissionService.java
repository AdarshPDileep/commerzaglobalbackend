package com.example.CommerzaGlobalBackend.commission.service;

import com.example.CommerzaGlobalBackend.commission.dto.CommissionPayoutConfigRequest;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionPayoutConfigResponse;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionRuleRequest;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionRuleResponse;
import com.example.CommerzaGlobalBackend.commission.dto.CommissionRuleStatusRequest;
import com.example.CommerzaGlobalBackend.commission.entity.BankTransferMode;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionCondition;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionPayoutConfig;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionRule;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionType;
import com.example.CommerzaGlobalBackend.commission.entity.FranchiseLevel;
import com.example.CommerzaGlobalBackend.commission.entity.PayoutCutoffDay;
import com.example.CommerzaGlobalBackend.commission.entity.PayoutFrequency;
import com.example.CommerzaGlobalBackend.commission.repository.CommissionPayoutConfigRepository;
import com.example.CommerzaGlobalBackend.commission.repository.CommissionRuleRepository;
import com.example.CommerzaGlobalBackend.common.exception.ResourceNotFoundException;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CommissionService {

    private static final LocalDate MIN_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final CommissionRuleRepository commissionRuleRepository;
    private final CommissionPayoutConfigRepository payoutConfigRepository;

    public CommissionService(CommissionRuleRepository commissionRuleRepository,
                             CommissionPayoutConfigRepository payoutConfigRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.payoutConfigRepository = payoutConfigRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CommissionRuleResponse> getRules(int page, int size, String status, FranchiseLevel level,
                                                         CommissionType type, String search) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<CommissionRule> result = commissionRuleRepository.search(level, type, parseStatus(status), cleanSearch(search), pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toRuleResponse).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public CommissionRuleResponse getRule(Long id) {
        return toRuleResponse(findRule(id));
    }

    public CommissionRuleResponse createRule(CommissionRuleRequest request) {
        validateRule(request, null);
        CommissionRule rule = CommissionRule.builder()
                .code(generateRuleCode())
                .franchiseLevel(request.franchiseLevel())
                .commissionType(request.commissionType())
                .value(request.value())
                .conditionType(request.conditionType())
                .minimumVolume(normalizedMinimumVolume(request))
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .active(request.active() == null || request.active())
                .build();
        preventActiveConflict(rule, null);
        return toRuleResponse(commissionRuleRepository.save(rule));
    }

    public CommissionRuleResponse updateRule(Long id, CommissionRuleRequest request) {
        CommissionRule rule = findRule(id);
        validateRule(request, id);
        rule.setFranchiseLevel(request.franchiseLevel());
        rule.setCommissionType(request.commissionType());
        rule.setValue(request.value());
        rule.setConditionType(request.conditionType());
        rule.setMinimumVolume(normalizedMinimumVolume(request));
        rule.setEffectiveFrom(request.effectiveFrom());
        rule.setEffectiveTo(request.effectiveTo());
        rule.setActive(request.active() == null || request.active());
        preventActiveConflict(rule, id);
        return toRuleResponse(rule);
    }

    public CommissionRuleResponse updateRuleStatus(Long id, CommissionRuleStatusRequest request) {
        CommissionRule rule = findRule(id);
        rule.setActive(required(request.active(), "Commission rule status is required."));
        preventActiveConflict(rule, id);
        return toRuleResponse(rule);
    }

    public CommissionRuleResponse deleteRule(Long id) {
        CommissionRule rule = findRule(id);
        CommissionRuleResponse response = toRuleResponse(rule);
        commissionRuleRepository.delete(rule);
        return response;
    }

    @Transactional(readOnly = true)
    public CommissionPayoutConfigResponse getPayoutConfig() {
        return payoutConfigRepository.findFirstByOrderByIdAsc()
                .map(this::toPayoutConfigResponse)
                .orElseGet(() -> toPayoutConfigResponse(defaultPayoutConfig()));
    }

    public CommissionPayoutConfigResponse updatePayoutConfig(CommissionPayoutConfigRequest request) {
        CommissionPayoutConfig config = payoutConfigRepository.findFirstByOrderByIdAsc().orElseGet(CommissionPayoutConfig::new);
        config.setFrequency(required(request.frequency(), "Payout frequency is required."));
        config.setCutoffDay(required(request.cutoffDay(), "Payout cutoff day is required."));
        config.setProcessingDays(nonNegative(required(request.processingDays(), "Processing days is required."), "Processing days cannot be negative."));
        config.setMinimumPayout(nonNegative(required(request.minimumPayout(), "Minimum payout is required."), "Minimum payout cannot be negative."));
        config.setBankTransferMode(required(request.bankTransferMode(), "Bank transfer mode is required."));
        config.setActive(request.active() == null || request.active());
        return toPayoutConfigResponse(payoutConfigRepository.save(config));
    }

    private void validateRule(CommissionRuleRequest request, Long existingId) {
        required(request.franchiseLevel(), "Franchise level is required.");
        required(request.commissionType(), "Commission type is required.");
        BigDecimal value = positive(required(request.value(), "Commission value is required."), "Commission value must be greater than zero.");
        required(request.conditionType(), "Commission condition is required.");
        if (request.commissionType() == CommissionType.PERCENTAGE && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage commission cannot exceed 100.");
        }
        if (request.conditionType() == CommissionCondition.VOLUME_TARGET && (request.minimumVolume() == null || request.minimumVolume() <= 0)) {
            throw new IllegalArgumentException("Minimum volume is required for volume target commission rules.");
        }
        if (request.effectiveFrom() != null && request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new IllegalArgumentException("Effective to date cannot be before effective from date.");
        }
    }

    private void preventActiveConflict(CommissionRule candidate, Long existingId) {
        if (!candidate.isActive()) return;
        List<CommissionRule> matches = commissionRuleRepository.findByActiveTrueAndFranchiseLevelAndConditionType(candidate.getFranchiseLevel(), candidate.getConditionType());
        boolean conflict = matches.stream()
                .filter(rule -> existingId == null || !rule.getId().equals(existingId))
                .anyMatch(rule -> periodsOverlap(rule.getEffectiveFrom(), rule.getEffectiveTo(), candidate.getEffectiveFrom(), candidate.getEffectiveTo()));
        if (conflict) {
            throw new IllegalArgumentException("An active commission rule already exists for this franchise level, condition, and effective period.");
        }
    }

    private boolean periodsOverlap(LocalDate firstFrom, LocalDate firstTo, LocalDate secondFrom, LocalDate secondTo) {
        LocalDate aFrom = firstFrom == null ? MIN_DATE : firstFrom;
        LocalDate aTo = firstTo == null ? MAX_DATE : firstTo;
        LocalDate bFrom = secondFrom == null ? MIN_DATE : secondFrom;
        LocalDate bTo = secondTo == null ? MAX_DATE : secondTo;
        return !aFrom.isAfter(bTo) && !bFrom.isAfter(aTo);
    }

    private Integer normalizedMinimumVolume(CommissionRuleRequest request) {
        return request.conditionType() == CommissionCondition.VOLUME_TARGET ? request.minimumVolume() : null;
    }

    private CommissionRule findRule(Long id) {
        return commissionRuleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Commission rule not found."));
    }

    private CommissionRuleResponse toRuleResponse(CommissionRule rule) {
        return new CommissionRuleResponse(
                rule.getId(),
                rule.getCode(),
                rule.getFranchiseLevel(),
                rule.getCommissionType(),
                rule.getValue(),
                rule.getConditionType(),
                rule.getMinimumVolume(),
                rule.getEffectiveFrom(),
                rule.getEffectiveTo(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    private CommissionPayoutConfig defaultPayoutConfig() {
        return CommissionPayoutConfig.builder()
                .frequency(PayoutFrequency.WEEKLY)
                .cutoffDay(PayoutCutoffDay.SUNDAY)
                .processingDays(2)
                .minimumPayout(BigDecimal.valueOf(500))
                .bankTransferMode(BankTransferMode.NEFT)
                .active(true)
                .build();
    }

    private CommissionPayoutConfigResponse toPayoutConfigResponse(CommissionPayoutConfig config) {
        return new CommissionPayoutConfigResponse(
                config.getId(),
                config.getFrequency(),
                config.getCutoffDay(),
                config.getProcessingDays(),
                config.getMinimumPayout(),
                config.getBankTransferMode(),
                config.isActive(),
                config.getUpdatedAt()
        );
    }

    private String generateRuleCode() {
        long next = Math.max(commissionRuleRepository.count() + 1, 1);
        String code;
        do {
            code = "COM" + String.format("%04d", next++);
        } while (commissionRuleRepository.existsByCode(code));
        return code;
    }

    private Boolean parseStatus(String status) {
        String clean = clean(status);
        if (clean == null) return null;
        if ("active".equalsIgnoreCase(clean)) return true;
        if ("inactive".equalsIgnoreCase(clean)) return false;
        return Boolean.valueOf(clean);
    }

    private String cleanSearch(String value) {
        String clean = clean(value);
        return clean == null ? null : "%" + clean.toLowerCase() + "%";
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private <T> T required(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private BigDecimal positive(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(message);
        return value;
    }

    private <T extends Number> T nonNegative(T value, String message) {
        if (value.doubleValue() < 0) throw new IllegalArgumentException(message);
        return value;
    }

    private BigDecimal nonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(message);
        return value;
    }
}
