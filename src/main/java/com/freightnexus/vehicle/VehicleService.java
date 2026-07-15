package com.freightnexus.vehicle;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.ForbiddenException;
import com.freightnexus.common.PageResponse;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.partner.Partner;
import com.freightnexus.partner.PartnerRepository;
import com.freightnexus.partner.PartnerType;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final PartnerRepository partnerRepository;

    public VehicleService(VehicleRepository vehicleRepository, PartnerRepository partnerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.partnerRepository = partnerRepository;
    }

    public PageResponse<VehicleDTO.Response> findMyVehicles(Pageable pageable) {
        return PageResponse.from(
                vehicleRepository.findByCarrier_Id(caller().id(), pageable).map(this::toResponse));
    }

    public VehicleDTO.Response findById(Long id) {
        return toResponse(getVehicle(id));
    }

    @Transactional
    public VehicleDTO.Response create(VehicleDTO.Request request) {
        FreightPrincipal me = caller();
        Partner carrier = partnerRepository.findById(me.id())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + me.id()));
        if (carrier.getType() != PartnerType.CARRIER) {
            throw new ForbiddenException("Only CARRIER partners can register vehicles");
        }
        Vehicle v = new Vehicle();
        v.setCarrier(carrier);
        v.setPlateNumber(request.plateNumber());
        v.setType(request.type());
        v.setWeightCapacityKg(request.weightCapacityKg());
        v.setVolumeCapacityM3(request.volumeCapacityM3());
        return toResponse(vehicleRepository.save(v));
    }

    private Vehicle getVehicle(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
        if (!v.getCarrier().getId().equals(caller().id())) {
            throw new ForbiddenException("Vehicle belongs to a different carrier");
        }
        return v;
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private VehicleDTO.Response toResponse(Vehicle v) {
        return new VehicleDTO.Response(v.getId(), v.getCarrier().getId(), v.getCarrier().getName(),
                v.getPlateNumber(), v.getType(), v.getWeightCapacityKg(),
                v.getVolumeCapacityM3(), v.getStatus(), v.getCreatedAt());
    }
}
