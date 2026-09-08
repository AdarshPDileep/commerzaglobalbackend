package com.example.CommerzaGlobalBackend.network.repository;

import com.example.CommerzaGlobalBackend.network.entity.NetworkRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NetworkRouteRepository extends JpaRepository<NetworkRoute, Long> {

    boolean existsByRouteCode(String routeCode);
    boolean existsByOriginIdAndDestinationId(Long originId, Long destinationId);
    Optional<NetworkRoute> findByOriginIdAndDestinationId(Long originId, Long destinationId);
    long countByActiveTrue();
    long countByOriginIdOrDestinationId(Long originId, Long destinationId);

    @Query("""
            select r from NetworkRoute r
            where (:active is null or r.active = :active)
              and (:searchPattern is null
                   or lower(r.routeCode) like :searchPattern
                   or lower(r.origin.name) like :searchPattern
                   or lower(r.destination.name) like :searchPattern)
            order by r.routeCode asc
            """)
    Page<NetworkRoute> search(@Param("active") Boolean active,
                              @Param("searchPattern") String searchPattern,
                              Pageable pageable);
}

