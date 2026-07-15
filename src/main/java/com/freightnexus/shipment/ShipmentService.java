package com.freightnexus.shipment;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.PageResponse;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.partner.Partner;
import com.freightnexus.partner.PartnerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final PartnerRepository partnerRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, PartnerRepository partnerRepository) {
        this.shipmentRepository = shipmentRepository;
        this.partnerRepository = partnerRepository;
    }

    public PageResponse<ShipmentDTO.Response> findMine(Pageable pageable) {
        return PageResponse.from(shipmentRepository.findByShipper_Id(caller().id(), pageable).map(this::toResponse));
    }

    public ShipmentDTO.Response findById(Long id) {
        return toResponse(shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id)));
    }

    @Transactional
    public ShipmentDTO.Response create(ShipmentDTO.Request request) {
        Partner shipper = partnerRepository.findById(caller().id())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        Shipment s = new Shipment();
        s.setShipper(shipper);
        s.setDescription(request.description());
        s.setWeightKg(request.weightKg());
        s.setVolumeM3(request.volumeM3());
        s.setHazmat(request.hazmat());
        s.setDeclaredValueUsd(request.declaredValueUsd());
        return toResponse(shipmentRepository.save(s));
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ShipmentDTO.Response toResponse(Shipment s) {
        return new ShipmentDTO.Response(s.getId(), s.getShipper().getId(), s.getShipper().getName(),
                s.getDescription(), s.getWeightKg(), s.getVolumeM3(), s.isHazmat(),
                s.getDeclaredValueUsd(), s.getStatus(), s.getCreatedAt());
    }
}
