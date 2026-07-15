package com.freightnexus.hos;

import com.freightnexus.driver.Driver;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "hos_windows")
public class HOSWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "window_date", nullable = false)
    private LocalDate windowDate;

    @Column(name = "driving_minutes", nullable = false)
    private int drivingMinutes = 0;

    @Column(name = "on_duty_minutes", nullable = false)
    private int onDutyMinutes = 0;

    @Column(name = "consecutive_driving_minutes", nullable = false)
    private int consecutiveDrivingMinutes = 0;

    @Column(name = "on_duty_start_at")
    private Instant onDutyStartAt;

    @Column(name = "last_break_at")
    private Instant lastBreakAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public LocalDate getWindowDate() { return windowDate; }
    public void setWindowDate(LocalDate windowDate) { this.windowDate = windowDate; }
    public int getDrivingMinutes() { return drivingMinutes; }
    public void setDrivingMinutes(int drivingMinutes) { this.drivingMinutes = drivingMinutes; }
    public int getOnDutyMinutes() { return onDutyMinutes; }
    public void setOnDutyMinutes(int onDutyMinutes) { this.onDutyMinutes = onDutyMinutes; }
    public int getConsecutiveDrivingMinutes() { return consecutiveDrivingMinutes; }
    public void setConsecutiveDrivingMinutes(int m) { this.consecutiveDrivingMinutes = m; }
    public Instant getOnDutyStartAt() { return onDutyStartAt; }
    public void setOnDutyStartAt(Instant onDutyStartAt) { this.onDutyStartAt = onDutyStartAt; }
    public Instant getLastBreakAt() { return lastBreakAt; }
    public void setLastBreakAt(Instant lastBreakAt) { this.lastBreakAt = lastBreakAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
