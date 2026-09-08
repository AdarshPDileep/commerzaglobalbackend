package com.example.CommerzaGlobalBackend.ratecard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "rate_card_slabs", indexes = {
        @Index(name = "idx_rate_card_slabs_card_scope", columnList = "rate_card_id, scope")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCardSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rate_card_id")
    private RateCard rateCard;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal minWeightKg;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal maxWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RateScope scope;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rate;
}
