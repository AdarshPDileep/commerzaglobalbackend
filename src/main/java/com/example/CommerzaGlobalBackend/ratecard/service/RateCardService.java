package com.example.CommerzaGlobalBackend.ratecard.service;

import com.example.CommerzaGlobalBackend.common.exception.ResourceNotFoundException;
import com.example.CommerzaGlobalBackend.geography.dto.PageResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardSlabRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardSlabResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.RateCardStatusRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.SurchargeRequest;
import com.example.CommerzaGlobalBackend.ratecard.dto.SurchargeResponse;
import com.example.CommerzaGlobalBackend.ratecard.dto.SurchargeStatusRequest;
import com.example.CommerzaGlobalBackend.ratecard.entity.RateCard;
import com.example.CommerzaGlobalBackend.ratecard.entity.RateCardSlab;
import com.example.CommerzaGlobalBackend.ratecard.entity.RateCardType;
import com.example.CommerzaGlobalBackend.ratecard.entity.RateScope;
import com.example.CommerzaGlobalBackend.ratecard.entity.Surcharge;
import com.example.CommerzaGlobalBackend.ratecard.repository.RateCardRepository;
import com.example.CommerzaGlobalBackend.ratecard.repository.SurchargeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class RateCardService {

    private final RateCardRepository rateCardRepository;
    private final SurchargeRepository surchargeRepository;

    public RateCardService(RateCardRepository rateCardRepository, SurchargeRepository surchargeRepository) {
        this.rateCardRepository = rateCardRepository;
        this.surchargeRepository = surchargeRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<RateCardResponse> getRateCards(int page, int size, RateCardType type, Boolean active, String search) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<RateCard> result = rateCardRepository.search(type, active, cleanSearch(search), pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toRateCardResponse).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RateCardResponse getRateCard(Long id) {
        return toRateCardResponse(findRateCard(id));
    }

    public RateCardResponse createRateCard(RateCardRequest request) {
        List<RateCardSlab> slabs = validateAndBuildSlabs(request.slabs());
        RateCard rateCard = RateCard.builder()
                .code(generateRateCardCode())
                .name(required(request.name(), "Rate card name is required."))
                .type(required(request.type(), "Rate card type is required."))
                .active(request.active() == null || request.active())
                .build();
        rateCard.replaceSlabs(slabs);
        return toRateCardResponse(rateCardRepository.save(rateCard));
    }

    public RateCardResponse updateRateCard(Long id, RateCardRequest request) {
        RateCard rateCard = findRateCard(id);
        rateCard.setName(required(request.name(), "Rate card name is required."));
        rateCard.setType(required(request.type(), "Rate card type is required."));
        rateCard.setActive(request.active() == null || request.active());
        rateCard.replaceSlabs(validateAndBuildSlabs(request.slabs()));
        return toRateCardResponse(rateCard);
    }

    public RateCardResponse updateRateCardStatus(Long id, RateCardStatusRequest request) {
        RateCard rateCard = findRateCard(id);
        rateCard.setActive(required(request.active(), "Rate card status is required."));
        return toRateCardResponse(rateCard);
    }

    public RateCardResponse deleteRateCard(Long id) {
        RateCard rateCard = findRateCard(id);
        RateCardResponse response = toRateCardResponse(rateCard);
        rateCardRepository.delete(rateCard);
        return response;
    }

    @Transactional(readOnly = true)
    public List<SurchargeResponse> getSurcharges() {
        return surchargeRepository.findAllByOrderByCreatedAtDescIdDesc().stream().map(this::toSurchargeResponse).toList();
    }

    public SurchargeResponse createSurcharge(SurchargeRequest request) {
        Surcharge surcharge = Surcharge.builder()
                .name(required(request.name(), "Surcharge name is required."))
                .type(required(request.type(), "Surcharge type is required."))
                .value(nonNegative(required(request.value(), "Surcharge value is required."), "Surcharge value cannot be negative."))
                .enabled(request.enabled() == null || request.enabled())
                .build();
        return toSurchargeResponse(surchargeRepository.save(surcharge));
    }

    public SurchargeResponse updateSurcharge(Long id, SurchargeRequest request) {
        Surcharge surcharge = findSurcharge(id);
        surcharge.setName(required(request.name(), "Surcharge name is required."));
        surcharge.setType(required(request.type(), "Surcharge type is required."));
        surcharge.setValue(nonNegative(required(request.value(), "Surcharge value is required."), "Surcharge value cannot be negative."));
        surcharge.setEnabled(request.enabled() == null || request.enabled());
        return toSurchargeResponse(surcharge);
    }

    public SurchargeResponse updateSurchargeStatus(Long id, SurchargeStatusRequest request) {
        Surcharge surcharge = findSurcharge(id);
        surcharge.setEnabled(required(request.enabled(), "Surcharge status is required."));
        return toSurchargeResponse(surcharge);
    }

    public SurchargeResponse deleteSurcharge(Long id) {
        Surcharge surcharge = findSurcharge(id);
        SurchargeResponse response = toSurchargeResponse(surcharge);
        surchargeRepository.delete(surcharge);
        return response;
    }

    private List<RateCardSlab> validateAndBuildSlabs(List<RateCardSlabRequest> requests) {
        if (requests == null || requests.isEmpty()) throw new IllegalArgumentException("At least one weight slab is required.");

        List<RateCardSlabRequest> sorted = requests.stream()
                .peek(this::validateSlab)
                .sorted(Comparator.comparing(RateCardSlabRequest::scope).thenComparing(RateCardSlabRequest::minWeightKg))
                .toList();

        for (int i = 1; i < sorted.size(); i++) {
            RateCardSlabRequest previous = sorted.get(i - 1);
            RateCardSlabRequest current = sorted.get(i);
            if (previous.scope() == current.scope() && previous.maxWeightKg().compareTo(current.minWeightKg()) > 0) {
                throw new IllegalArgumentException("Weight slabs cannot overlap for the same pricing scope.");
            }
        }

        return sorted.stream()
                .map(request -> RateCardSlab.builder()
                        .minWeightKg(request.minWeightKg())
                        .maxWeightKg(request.maxWeightKg())
                        .scope(request.scope())
                        .rate(request.rate())
                        .build())
                .toList();
    }

    private void validateSlab(RateCardSlabRequest slab) {
        if (slab == null) throw new IllegalArgumentException("Weight slab is required.");
        BigDecimal min = required(slab.minWeightKg(), "Minimum weight is required.");
        BigDecimal max = required(slab.maxWeightKg(), "Maximum weight is required.");
        required(slab.scope(), "Pricing scope is required.");
        nonNegative(required(slab.rate(), "Rate is required."), "Rate cannot be negative.");
        if (min.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Minimum weight cannot be negative.");
        if (min.compareTo(max) >= 0) throw new IllegalArgumentException("Minimum weight must be less than maximum weight.");
    }

    private RateCardResponse toRateCardResponse(RateCard rateCard) {
        return new RateCardResponse(
                rateCard.getId(),
                rateCard.getCode(),
                rateCard.getName(),
                rateCard.getType(),
                rateCard.isActive(),
                rateCard.getType() == RateCardType.DEFAULT ? "All Sellers" : "Seller Specific",
                rateCard.getSlabs().stream().map(this::toSlabResponse).toList(),
                rateCard.getCreatedAt(),
                rateCard.getUpdatedAt()
        );
    }

    private RateCardSlabResponse toSlabResponse(RateCardSlab slab) {
        return new RateCardSlabResponse(slab.getId(), slab.getMinWeightKg(), slab.getMaxWeightKg(), slab.getScope(), slab.getRate());
    }

    private SurchargeResponse toSurchargeResponse(Surcharge surcharge) {
        return new SurchargeResponse(surcharge.getId(), surcharge.getName(), surcharge.getType(), surcharge.getValue(), surcharge.isEnabled(), surcharge.getCreatedAt(), surcharge.getUpdatedAt());
    }

    private RateCard findRateCard(Long id) {
        return rateCardRepository.findWithSlabsById(id).orElseThrow(() -> new ResourceNotFoundException("Rate card not found."));
    }

    private Surcharge findSurcharge(Long id) {
        return surchargeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Surcharge not found."));
    }

    private String generateRateCardCode() {
        long next = Math.max(rateCardRepository.count() + 1, 1);
        String code;
        do {
            code = "RC" + String.format("%04d", next++);
        } while (rateCardRepository.existsByCode(code));
        return code;
    }

    private String cleanSearch(String value) {
        String clean = clean(value);
        return clean == null ? null : "%" + clean.toLowerCase() + "%";
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private <T> T required(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private BigDecimal nonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(message);
        return value;
    }
}
