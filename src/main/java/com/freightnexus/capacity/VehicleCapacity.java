package com.freightnexus.capacity;

import com.freightnexus.vehicle.Vehicle;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_capacity")
public class VehicleCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "capacity_date", nullable = false)
    private LocalDate capacityDate;

    @Column(name = "total_weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalWeightKg;

    @Column(name = "booked_weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal bookedWeightKg = BigDecimal.ZERO;

    @Column(name = "total_volume_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalVolumeM3;

    @Column(name = "booked_volume_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal bookedVolumeM3 = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public LocalDate getCapacityDate() { return capacityDate; }
    public void setCapacityDate(LocalDate capacityDate) { this.capacityDate = capacityDate; }
    public BigDecimal getTotalWeightKg() { return totalWeightKg; }
    public void setTotalWeightKg(BigDecimal totalWeightKg) { this.totalWeightKg = totalWeightKg; }
    public BigDecimal getBookedWeightKg() { return bookedWeightKg; }
    public void setBookedWeightKg(BigDecimal bookedWeightKg) { this.bookedWeightKg = bookedWeightKg; }
    public BigDecimal getTotalVolumeM3() { return totalVolumeM3; }
    public void setTotalVolumeM3(BigDecimal totalVolumeM3) { this.totalVolumeM3 = totalVolumeM3; }
    public BigDecimal getBookedVolumeM3() { return bookedVolumeM3; }
    public void setBookedVolumeM3(BigDecimal bookedVolumeM3) { this.bookedVolumeM3 = bookedVolumeM3; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public BigDecimal availableWeightKg() { return totalWeightKg.subtract(bookedWeightKg); }
    public BigDecimal availableVolumeM3() { return totalVolumeM3.subtract(bookedVolumeM3); }
}
