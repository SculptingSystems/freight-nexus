package com.freightnexus.capacity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface VehicleCapacityRepository extends JpaRepository<VehicleCapacity, Long> {

    Optional<VehicleCapacity> findByVehicle_IdAndCapacityDate(Long vehicleId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT vc FROM VehicleCapacity vc WHERE vc.vehicle.id = :vehicleId AND vc.capacityDate = :date")
    Optional<VehicleCapacity> lockForBooking(@Param("vehicleId") Long vehicleId, @Param("date") LocalDate date);
}
