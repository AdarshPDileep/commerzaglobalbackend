package com.example.CommerzaGlobalBackend.seller.repository;

import com.example.CommerzaGlobalBackend.seller.entity.SellerBusinessDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerBusinessDetailsRepository extends JpaRepository<SellerBusinessDetails, Long> {
    Optional<SellerBusinessDetails> findBySellerId(Long sellerId);
    boolean existsBySellerId(Long sellerId);
}
