package com.example.CommerzaGlobalBackend.ratecard.repository;

import com.example.CommerzaGlobalBackend.ratecard.entity.Surcharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurchargeRepository extends JpaRepository<Surcharge, Long> {

    List<Surcharge> findAllByOrderByCreatedAtDescIdDesc();
}
