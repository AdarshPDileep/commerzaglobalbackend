package com.example.CommerzaGlobalBackend.seller.repository;

import com.example.CommerzaGlobalBackend.seller.entity.SellerKycDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerKycDetailsRepository extends JpaRepository<SellerKycDetails, Long> {
    Optional<SellerKycDetails> findBySellerId(Long sellerId);
    boolean existsBySellerId(Long sellerId);
}
