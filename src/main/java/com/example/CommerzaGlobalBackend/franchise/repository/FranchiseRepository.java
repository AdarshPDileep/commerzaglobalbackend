package com.example.CommerzaGlobalBackend.franchise.repository;

import com.example.CommerzaGlobalBackend.franchise.entity.Franchise;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseStatus;
import com.example.CommerzaGlobalBackend.franchise.entity.FranchiseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    boolean existsByCode(String code);

    @Query("""
            select f from Franchise f
            where (:type is null or f.type = :type)
              and (:status is null or f.status = :status)
              and (:searchPattern is null
                   or lower(f.name) like :searchPattern
                   or lower(f.code) like :searchPattern
                   or lower(f.ownerName) like :searchPattern
                   or lower(f.phone) like :searchPattern)
            order by f.createdAt desc, f.id desc
            """)
    Page<Franchise> search(@Param("type") FranchiseType type,
                           @Param("status") FranchiseStatus status,
                           @Param("searchPattern") String searchPattern,
                           Pageable pageable);
}
