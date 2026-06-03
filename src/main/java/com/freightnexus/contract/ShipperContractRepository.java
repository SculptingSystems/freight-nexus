package com.freightnexus.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShipperContractRepository extends JpaRepository<ShipperContract, Long> {

    @Query("SELECT c FROM ShipperContract c JOIN FETCH c.carrier JOIN FETCH c.shipper WHERE c.id = :id")
    Optional<ShipperContract> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM ShipperContract c JOIN FETCH c.carrier JOIN FETCH c.shipper WHERE c.carrier.id = :id OR c.shipper.id = :id")
    List<ShipperContract> findByPartnerId(@Param("id") Long partnerId);

    boolean existsByCarrier_IdAndShipper_IdAndStatus(Long carrierId, Long shipperId, ContractStatus status);

    boolean existsByCarrier_IdAndStatus(Long carrierId, ContractStatus status);
    boolean existsByShipper_IdAndStatus(Long shipperId, ContractStatus status);
}
