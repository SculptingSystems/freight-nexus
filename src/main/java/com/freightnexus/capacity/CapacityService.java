package com.freightnexus.capacity;

import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.vehicle.Vehicle;
import com.freightnexus.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CapacityService {

    private final VehicleCapacityRepository capacityRepository;
    private final VehicleRepository vehicleRepository;

    public CapacityService(VehicleCapacityRepository capacityRepository, VehicleRepository vehicleRepository) {
        this.capacityRepository = capacityRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public CapacityDTO.Response setCapacity(Long vehicleId, CapacityDTO.Request request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        VehicleCapacity capacity = capacityRepository
                .findByVehicle_IdAndCapacityDate(vehicleId, request.capacityDate())
                .orElseGet(() -> {
                    VehicleCapacity vc = new VehicleCapacity();
                    vc.setVehicle(vehicle);
                    vc.setCapacityDate(request.capacityDate());
                    return vc;
                });

        capacity.setTotalWeightKg(request.totalWeightKg());
        capacity.setTotalVolumeM3(request.totalVolumeM3());
        return toResponse(capacityRepository.save(capacity));
    }

    public CapacityDTO.Response getCapacity(Long vehicleId, java.time.LocalDate date) {
        return toResponse(capacityRepository.findByVehicle_IdAndCapacityDate(vehicleId, date)
                .orElseThrow(() -> new ResourceNotFoundException("No capacity set for vehicle " + vehicleId + " on " + date)));
    }

    private CapacityDTO.Response toResponse(VehicleCapacity vc) {
        return new CapacityDTO.Response(
                vc.getId(), vc.getVehicle().getId(), vc.getVehicle().getPlateNumber(),
                vc.getCapacityDate(), vc.getTotalWeightKg(), vc.getBookedWeightKg(), vc.availableWeightKg(),
                vc.getTotalVolumeM3(), vc.getBookedVolumeM3(), vc.availableVolumeM3());
    }
}
