package com.freightnexus.vehicle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Page<Vehicle> findByCarrier_Id(Long carrierId, Pageable pageable);
    List<Vehicle> findByCarrier_IdAndStatus(Long carrierId, VehicleStatus status);
}
