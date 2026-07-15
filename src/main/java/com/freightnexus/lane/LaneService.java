package com.freightnexus.lane;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.PageResponse;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.partner.Partner;
import com.freightnexus.partner.PartnerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LaneService {

    private final LaneRepository laneRepository;
    private final PartnerRepository partnerRepository;

    public LaneService(LaneRepository laneRepository, PartnerRepository partnerRepository) {
        this.laneRepository = laneRepository;
        this.partnerRepository = partnerRepository;
    }

    public PageResponse<LaneDTO.Response> findMyLanes(Pageable pageable) {
        return PageResponse.from(laneRepository.findByCarrier_Id(caller().id(), pageable).map(this::toResponse));
    }

    public List<LaneDTO.Response> search(String origin, String destination) {
        return laneRepository.searchLanes(origin, destination).stream().map(this::toResponse).toList();
    }

    public LaneDTO.Response findById(Long id) {
        return toResponse(laneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane not found: " + id)));
    }

    @Transactional
    public LaneDTO.Response create(LaneDTO.Request request) {
        Partner carrier = partnerRepository.findById(caller().id())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found"));
        Lane lane = new Lane();
        lane.setCarrier(carrier);
        lane.setOriginCity(request.originCity());
        lane.setOriginCountry(request.originCountry());
        lane.setOriginLat(request.originLat());
        lane.setOriginLon(request.originLon());
        lane.setDestinationCity(request.destinationCity());
        lane.setDestinationCountry(request.destinationCountry());
        lane.setDestinationLat(request.destinationLat());
        lane.setDestinationLon(request.destinationLon());
        lane.setDistanceKm(request.distanceKm());
        lane.setEstimatedHours(request.estimatedHours());
        return toResponse(laneRepository.save(lane));
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private LaneDTO.Response toResponse(Lane l) {
        return new LaneDTO.Response(l.getId(), l.getCarrier().getId(), l.getCarrier().getName(),
                l.getOriginCity(), l.getOriginCountry(), l.getDestinationCity(), l.getDestinationCountry(),
                l.getDistanceKm(), l.getEstimatedHours(), l.getStatus(), l.getCreatedAt());
    }
}
