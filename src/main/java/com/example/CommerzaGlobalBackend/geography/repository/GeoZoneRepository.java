package com.example.CommerzaGlobalBackend.geography.repository;

import com.example.CommerzaGlobalBackend.geography.entity.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
    List<GeoZone> findByStateIdOrderByNameAsc(Long stateId);
    boolean existsByStateIdAndNameIgnoreCase(Long stateId, String name);
    long countByStateId(Long stateId);
    List<GeoZone> findTop10ByNameContainingIgnoreCase(String name);
}
