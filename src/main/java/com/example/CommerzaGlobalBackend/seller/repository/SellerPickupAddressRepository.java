package com.example.CommerzaGlobalBackend.seller.repository;

import com.example.CommerzaGlobalBackend.seller.entity.SellerPickupAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerPickupAddressRepository extends JpaRepository<SellerPickupAddress, Long> {
    Optional<SellerPickupAddress> findBySellerId(Long sellerId);
    boolean existsBySellerId(Long sellerId);
}
