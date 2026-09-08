package com.example.CommerzaGlobalBackend.geography.repository;

import com.example.CommerzaGlobalBackend.geography.entity.GeoTaluk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoTalukRepository extends JpaRepository<GeoTaluk, Long> {
    List<GeoTaluk> findByDistrictIdOrderByNameAsc(Long districtId);
    boolean existsByDistrictIdAndNameIgnoreCase(Long districtId, String name);
    long countByDistrictId(Long districtId);
    List<GeoTaluk> findTop10ByNameContainingIgnoreCase(String name);
}
