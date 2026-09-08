package com.example.CommerzaGlobalBackend.network.repository;

import com.example.CommerzaGlobalBackend.network.entity.NetworkNodePincode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NetworkNodePincodeRepository extends JpaRepository<NetworkNodePincode, Long> {
    List<NetworkNodePincode> findByNodeIdOrderByPincodePincodeAsc(Long nodeId);
    Optional<NetworkNodePincode> findByNodeIdAndPincodeId(Long nodeId, Long pincodeId);
    long countByNodeId(Long nodeId);
    void deleteByNodeIdAndPincodeId(Long nodeId, Long pincodeId);
}
