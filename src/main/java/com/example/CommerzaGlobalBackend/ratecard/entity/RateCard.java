package com.example.CommerzaGlobalBackend.ratecard.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rate_cards", indexes = {
        @Index(name = "idx_rate_cards_code", columnList = "code"),
        @Index(name = "idx_rate_cards_type", columnList = "type"),
        @Index(name = "idx_rate_cards_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RateCardType type;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "rateCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("scope asc, minWeightKg asc")
    @Builder.Default
    private List<RateCardSlab> slabs = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (type == null) type = RateCardType.DEFAULT;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void replaceSlabs(List<RateCardSlab> nextSlabs) {
        slabs.clear();
        nextSlabs.forEach(slab -> {
            slab.setRateCard(this);
            slabs.add(slab);
        });
    }
}
