package com.example.CommerzaGlobalBackend.seller.repository;

import com.example.CommerzaGlobalBackend.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobile(String mobile);
    boolean existsBySellerCode(String sellerCode);
}
