package com.example.CommerzaGlobalBackend.seller.repository;

import com.example.CommerzaGlobalBackend.seller.entity.OtpPurpose;
import com.example.CommerzaGlobalBackend.seller.entity.SellerOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerOtpRepository extends JpaRepository<SellerOtp, Long> {
    Optional<SellerOtp> findTopBySellerIdAndPurposeOrderByCreatedAtDesc(Long sellerId, OtpPurpose purpose);
}
