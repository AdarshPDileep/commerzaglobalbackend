package com.example.CommerzaGlobalBackend.seller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "sellers", indexes = {
        @Index(name = "idx_sellers_code", columnList = "seller_code"),
        @Index(name = "idx_sellers_email", columnList = "email"),
        @Index(name = "idx_sellers_mobile", columnList = "mobile"),
        @Index(name = "idx_sellers_application_status", columnList = "application_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_code", nullable = false, unique = true, length = 20)
    private String sellerCode;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String mobile;

    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private boolean mobileVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean termsAccepted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 30)
    private SellerApplicationStatus applicationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    private SellerAccountStatus accountStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (applicationStatus == null) applicationStatus = SellerApplicationStatus.DRAFT;
        if (accountStatus == null) accountStatus = SellerAccountStatus.PENDING;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
