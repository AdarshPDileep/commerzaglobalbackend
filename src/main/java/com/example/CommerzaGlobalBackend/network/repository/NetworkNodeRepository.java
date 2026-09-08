package com.example.CommerzaGlobalBackend.network.repository;

import com.example.CommerzaGlobalBackend.network.entity.NetworkNode;
import com.example.CommerzaGlobalBackend.network.entity.NodeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {

    boolean existsByCode(String code);

    long countByType(NodeType type);

    @Query("select coalesce(sum(n.dailyCapacity), 0) from NetworkNode n")
    long sumDailyCapacity();

    @Query("""
            select n from NetworkNode n
            where (:type is null or n.type = :type)
              and (:active is null or n.active = :active)
              and (:searchPattern is null
                   or lower(n.name) like :searchPattern
                   or lower(n.code) like :searchPattern)
            order by n.name asc
            """)
    Page<NetworkNode> search(@Param("type") NodeType type,
                             @Param("active") Boolean active,
                             @Param("searchPattern") String searchPattern,
                             Pageable pageable);
}

