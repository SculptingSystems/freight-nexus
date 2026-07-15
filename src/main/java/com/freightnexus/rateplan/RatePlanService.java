package com.freightnexus.rateplan;

import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.lane.Lane;
import com.freightnexus.lane.LaneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RatePlanService {

    private final RatePlanRepository ratePlanRepository;
    private final LaneRepository laneRepository;

    public RatePlanService(RatePlanRepository ratePlanRepository, LaneRepository laneRepository) {
        this.ratePlanRepository = ratePlanRepository;
        this.laneRepository = laneRepository;
    }

    public List<RatePlanDTO.Response> findByLane(Long laneId) {
        if (!laneRepository.existsById(laneId)) {
            throw new ResourceNotFoundException("Lane not found: " + laneId);
        }
        return ratePlanRepository.findByLane_Id(laneId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RatePlanDTO.Response create(Long laneId, RatePlanDTO.Request request) {
        Lane lane = laneRepository.findById(laneId)
                .orElseThrow(() -> new ResourceNotFoundException("Lane not found: " + laneId));
        RatePlan rp = new RatePlan();
        rp.setLane(lane);
        rp.setName(request.name());
        rp.setPricingModel(request.pricingModel());
        rp.setBaseRate(request.baseRate());
        return toResponse(ratePlanRepository.save(rp));
    }

    private RatePlanDTO.Response toResponse(RatePlan rp) {
        return new RatePlanDTO.Response(rp.getId(), rp.getLane().getId(),
                rp.getLane().getOriginCity(), rp.getLane().getDestinationCity(),
                rp.getName(), rp.getPricingModel(), rp.getBaseRate(),
                rp.getStatus(), rp.getCreatedAt());
    }
}
