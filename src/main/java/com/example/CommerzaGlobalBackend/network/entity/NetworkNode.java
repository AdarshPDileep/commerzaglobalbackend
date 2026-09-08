package com.example.CommerzaGlobalBackend.network.entity;

import com.example.CommerzaGlobalBackend.geography.entity.GeoDistrict;
import com.example.CommerzaGlobalBackend.geography.entity.GeoPincode;
import com.example.CommerzaGlobalBackend.geography.entity.GeoState;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTaluk;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTown;
import com.example.CommerzaGlobalBackend.geography.entity.GeoZone;
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
@Table(name = "network_nodes", indexes = {
        @Index(name = "idx_network_nodes_type", columnList = "type"),
        @Index(name = "idx_network_nodes_active", columnList = "active"),
        @Index(name = "idx_network_nodes_state", columnList = "state_id"),
        @Index(name = "idx_network_nodes_zone", columnList = "zone_id"),
        @Index(name = "idx_network_nodes_district", columnList = "district_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NodeType type;

    @Column(nullable = false)
    private Integer dailyCapacity;

    private String address;
    private String contactName;
    private String contactPhone;
    private Long managerUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "state_id")
    private GeoState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id")
    private GeoZone zone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id")
    private GeoDistrict district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taluk_id")
    private GeoTaluk taluk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "town_id")
    private GeoTown town;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pincode_id")
    private GeoPincode pincode;

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
