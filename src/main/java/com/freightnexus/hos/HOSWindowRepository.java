package com.freightnexus.hos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HOSWindowRepository extends JpaRepository<HOSWindow, Long> {

    Optional<HOSWindow> findByDriver_IdAndWindowDate(Long driverId, LocalDate date);

    // Fetch last 8 days for rolling 70-hour calculation
    @Query("SELECT h FROM HOSWindow h WHERE h.driver.id = :driverId AND h.windowDate >= :since ORDER BY h.windowDate DESC")
    List<HOSWindow> findRecentWindows(@Param("driverId") Long driverId, @Param("since") LocalDate since);
}
