package com.example.CommerzaGlobalBackend.ratecard.repository;

import com.example.CommerzaGlobalBackend.ratecard.entity.RateCard;
import com.example.CommerzaGlobalBackend.ratecard.entity.RateCardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RateCardRepository extends JpaRepository<RateCard, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "slabs")
    Optional<RateCard> findWithSlabsById(Long id);

    @Query("""
            select r from RateCard r
            where (:type is null or r.type = :type)
              and (:active is null or r.active = :active)
              and (:searchPattern is null
                   or lower(r.name) like :searchPattern
                   or lower(r.code) like :searchPattern)
            order by r.createdAt desc, r.id desc
            """)
    Page<RateCard> search(@Param("type") RateCardType type,
                          @Param("active") Boolean active,
                          @Param("searchPattern") String searchPattern,
                          Pageable pageable);
}
