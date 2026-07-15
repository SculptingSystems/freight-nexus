package com.freightnexus.lane;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LaneRepository extends JpaRepository<Lane, Long> {
    Page<Lane> findByCarrier_Id(Long carrierId, Pageable pageable);

    @Query("""
            SELECT l FROM Lane l
            WHERE LOWER(l.originCity) = LOWER(:origin)
              AND LOWER(l.destinationCity) = LOWER(:destination)
              AND l.status = 'ACTIVE'
            """)
    List<Lane> searchLanes(@Param("origin") String origin, @Param("destination") String destination);
}
