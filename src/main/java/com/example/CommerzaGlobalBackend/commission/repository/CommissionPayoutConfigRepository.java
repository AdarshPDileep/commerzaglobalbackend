package com.example.CommerzaGlobalBackend.commission.repository;

import com.example.CommerzaGlobalBackend.commission.entity.CommissionPayoutConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionPayoutConfigRepository extends JpaRepository<CommissionPayoutConfig, Long> {

    Optional<CommissionPayoutConfig> findFirstByOrderByIdAsc();
}
