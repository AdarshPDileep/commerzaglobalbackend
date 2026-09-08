package com.example.CommerzaGlobalBackend.geography.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "geo_pincodes", indexes = {
        @Index(name = "idx_geo_pincodes_pincode", columnList = "pincode"),
        @Index(name = "idx_geo_pincodes_town", columnList = "town_id"),
        @Index(name = "idx_geo_pincodes_serviceable", columnList = "serviceable"),
        @Index(name = "idx_geo_pincodes_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoPincode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String pincode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "town_id")
    private GeoTown town;

    @Builder.Default
    @Column(nullable = false)
    private boolean serviceable = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean pickupAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean deliveryAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean codAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean prepaidAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean reversePickupAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
