package com.example.CommerzaGlobalBackend.geography.repository;

import com.example.CommerzaGlobalBackend.geography.entity.GeoTown;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoTownRepository extends JpaRepository<GeoTown, Long> {
    List<GeoTown> findByTalukIdOrderByNameAsc(Long talukId);
    boolean existsByTalukIdAndNameIgnoreCase(Long talukId, String name);
    long countByTalukId(Long talukId);
    boolean existsByTalukId(Long talukId);
    List<GeoTown> findTop10ByNameContainingIgnoreCase(String name);
}

