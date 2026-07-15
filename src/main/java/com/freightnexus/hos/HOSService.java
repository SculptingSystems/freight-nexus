package com.freightnexus.hos;

import com.freightnexus.common.HOSViolationException;
import com.freightnexus.driver.Driver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Enforces FMCSA Hours of Service rules:
 * Rule 1: 11-hour driving limit per day (resets after 10h consecutive off-duty)
 * Rule 2: 14-hour on-duty window from first on-duty moment
 * Rule 3: 30-minute break required after 8 consecutive driving hours
 * Rule 4: 70-hour limit over any rolling 8-day period
 */
@Service
@Transactional
public class HOSService {

    private static final int MAX_DAILY_DRIVING_MINUTES  = 11 * 60;
    private static final int MAX_ON_DUTY_WINDOW_MINUTES = 14 * 60;
    private static final int BREAK_REQUIRED_AFTER_MIN   = 8 * 60;
    private static final int BREAK_DURATION_MINUTES     = 30;
    private static final int MAX_ROLLING_8DAY_MINUTES   = 70 * 60;

    private final HOSWindowRepository hosWindowRepository;

    public HOSService(HOSWindowRepository hosWindowRepository) {
        this.hosWindowRepository = hosWindowRepository;
    }

    public void validateBeforeAssignment(Driver driver, int estimatedDrivingMinutes) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        HOSWindow window = hosWindowRepository
                .findByDriver_IdAndWindowDate(driver.getId(), today)
                .orElseGet(() -> createWindow(driver, today));

        // Rule 1: daily driving limit
        if (window.getDrivingMinutes() + estimatedDrivingMinutes > MAX_DAILY_DRIVING_MINUTES) {
            throw new HOSViolationException(
                    "Driver would exceed the 11-hour daily driving limit. "
                    + "Remaining today: " + (MAX_DAILY_DRIVING_MINUTES - window.getDrivingMinutes()) + " min.");
        }

        // Rule 2: 14-hour on-duty window
        if (window.getOnDutyStartAt() != null) {
            long onDutyElapsed = Duration.between(window.getOnDutyStartAt(), Instant.now()).toMinutes();
            if (onDutyElapsed + estimatedDrivingMinutes > MAX_ON_DUTY_WINDOW_MINUTES) {
                throw new HOSViolationException(
                        "Driver would exceed the 14-hour on-duty window. Window opened at: "
                        + window.getOnDutyStartAt());
            }
        }

        // Rule 3: 30-minute break after 8 consecutive hours
        if (window.getConsecutiveDrivingMinutes() >= BREAK_REQUIRED_AFTER_MIN) {
            throw new HOSViolationException(
                    "Driver has driven " + window.getConsecutiveDrivingMinutes()
                    + " consecutive minutes and requires a " + BREAK_DURATION_MINUTES + "-minute break.");
        }

        // Rule 4: 70-hour / 8-day rolling limit
        int rollingMinutes = getRolling8DayMinutes(driver.getId(), today);
        if (rollingMinutes + estimatedDrivingMinutes > MAX_ROLLING_8DAY_MINUTES) {
            throw new HOSViolationException(
                    "Driver would exceed the 70-hour/8-day rolling limit. "
                    + "Rolling total: " + rollingMinutes + " min, limit: " + MAX_ROLLING_8DAY_MINUTES + " min.");
        }
    }

    public void recordDrivingMinutes(Driver driver, int minutesDriven) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        HOSWindow window = hosWindowRepository
                .findByDriver_IdAndWindowDate(driver.getId(), today)
                .orElseGet(() -> createWindow(driver, today));

        window.setDrivingMinutes(window.getDrivingMinutes() + minutesDriven);
        window.setOnDutyMinutes(window.getOnDutyMinutes() + minutesDriven);
        window.setConsecutiveDrivingMinutes(window.getConsecutiveDrivingMinutes() + minutesDriven);

        if (window.getOnDutyStartAt() == null) {
            window.setOnDutyStartAt(Instant.now());
        }

        hosWindowRepository.save(window);
    }

    public void recordBreak(Driver driver) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        hosWindowRepository.findByDriver_IdAndWindowDate(driver.getId(), today).ifPresent(window -> {
            window.setConsecutiveDrivingMinutes(0);
            window.setLastBreakAt(Instant.now());
            hosWindowRepository.save(window);
        });
    }

    public HOSStatusDTO getStatus(Driver driver) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        HOSWindow window = hosWindowRepository
                .findByDriver_IdAndWindowDate(driver.getId(), today)
                .orElseGet(() -> createWindow(driver, today));

        int rolling = getRolling8DayMinutes(driver.getId(), today);
        return new HOSStatusDTO(
                window.getDrivingMinutes(),
                MAX_DAILY_DRIVING_MINUTES - window.getDrivingMinutes(),
                window.getConsecutiveDrivingMinutes(),
                BREAK_REQUIRED_AFTER_MIN - window.getConsecutiveDrivingMinutes(),
                rolling,
                MAX_ROLLING_8DAY_MINUTES - rolling
        );
    }

    private int getRolling8DayMinutes(Long driverId, LocalDate today) {
        List<HOSWindow> windows = hosWindowRepository.findRecentWindows(driverId, today.minusDays(7));
        return windows.stream().mapToInt(HOSWindow::getDrivingMinutes).sum();
    }

    private HOSWindow createWindow(Driver driver, LocalDate date) {
        HOSWindow w = new HOSWindow();
        w.setDriver(driver);
        w.setWindowDate(date);
        return hosWindowRepository.save(w);
    }

    public record HOSStatusDTO(
            int drivingMinutesToday,
            int drivingMinutesRemaining,
            int consecutiveDrivingMinutes,
            int minutesUntilBreakRequired,
            int rolling8DayMinutes,
            int rolling8DayRemaining
    ) {}
}
