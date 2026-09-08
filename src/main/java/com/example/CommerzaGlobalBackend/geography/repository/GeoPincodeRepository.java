package com.example.CommerzaGlobalBackend.geography.repository;

import com.example.CommerzaGlobalBackend.geography.entity.GeoPincode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeoPincodeRepository extends JpaRepository<GeoPincode, Long> {
    Optional<GeoPincode> findByPincode(String pincode);
    boolean existsByPincode(String pincode);
    Page<GeoPincode> findByTownIdOrderByPincodeAsc(Long townId, Pageable pageable);
    List<GeoPincode> findTop10ByPincodeContaining(String pincode);
    long countByTownId(Long townId);
    boolean existsByTownId(Long townId);
    long countByServiceableTrue();
}

