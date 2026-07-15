package com.freightnexus.rateplan;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {
    List<RatePlan> findByLane_Id(Long laneId);
    List<RatePlan> findByLane_IdAndStatus(Long laneId, String status);
}
