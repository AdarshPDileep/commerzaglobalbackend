package com.example.CommerzaGlobalBackend.commission.repository;

import com.example.CommerzaGlobalBackend.commission.entity.CommissionCondition;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionRule;
import com.example.CommerzaGlobalBackend.commission.entity.CommissionType;
import com.example.CommerzaGlobalBackend.commission.entity.FranchiseLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    boolean existsByCode(String code);

    List<CommissionRule> findByActiveTrueAndFranchiseLevelAndConditionType(FranchiseLevel franchiseLevel,
                                                                           CommissionCondition conditionType);

    @Query("""
            select r from CommissionRule r
            where (:level is null or r.franchiseLevel = :level)
              and (:type is null or r.commissionType = :type)
              and (:active is null or r.active = :active)
              and (:searchPattern is null
                   or lower(r.code) like :searchPattern
                   or lower(cast(r.franchiseLevel as string)) like :searchPattern
                   or lower(cast(r.conditionType as string)) like :searchPattern)
            order by r.createdAt desc, r.id desc
            """)
    Page<CommissionRule> search(@Param("level") FranchiseLevel level,
                                @Param("type") CommissionType type,
                                @Param("active") Boolean active,
                                @Param("searchPattern") String searchPattern,
                                Pageable pageable);
}
