package com.freightnexus.load;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoadStatusHistoryRepository extends JpaRepository<LoadStatusHistory, Long> {
    List<LoadStatusHistory> findByLoad_IdOrderByOccurredAtAsc(Long loadId);
}
