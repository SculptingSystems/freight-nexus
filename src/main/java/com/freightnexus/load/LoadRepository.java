package com.freightnexus.load;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoadRepository extends JpaRepository<Load, Long> {

    @Query("""
            SELECT l FROM Load l
            JOIN FETCH l.shipment s
            JOIN FETCH s.shipper
            JOIN FETCH l.vehicle v
            JOIN FETCH v.carrier
            JOIN FETCH l.driver
            JOIN FETCH l.ratePlan rp
            JOIN FETCH rp.lane
            WHERE l.id = :id
            """)
    Optional<Load> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"shipment", "shipment.shipper", "vehicle", "vehicle.carrier", "driver", "ratePlan", "ratePlan.lane"})
    Page<Load> findByShipment_Shipper_Id(Long shipperId, Pageable pageable);

    @EntityGraph(attributePaths = {"shipment", "shipment.shipper", "vehicle", "vehicle.carrier", "driver", "ratePlan", "ratePlan.lane"})
    Page<Load> findByVehicle_Carrier_Id(Long carrierId, Pageable pageable);
}
