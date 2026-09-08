package com.example.CommerzaGlobalBackend.geography.repository;

import com.example.CommerzaGlobalBackend.geography.entity.GeoState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoStateRepository extends JpaRepository<GeoState, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<GeoState> findTop10ByNameContainingIgnoreCase(String name);
}
