package com.example.CommerzaGlobalBackend.geography.repository;

import com.example.CommerzaGlobalBackend.geography.entity.GeoDistrict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoDistrictRepository extends JpaRepository<GeoDistrict, Long> {
    List<GeoDistrict> findByZoneIdOrderByNameAsc(Long zoneId);
    boolean existsByZoneIdAndNameIgnoreCase(Long zoneId, String name);
    long countByZoneId(Long zoneId);
    List<GeoDistrict> findTop10ByNameContainingIgnoreCase(String name);
}
