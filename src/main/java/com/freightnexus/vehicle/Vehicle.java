package com.freightnexus.vehicle;

import com.freightnexus.partner.Partner;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vehicles")
@SQLRestriction("deleted_at IS NULL")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Partner carrier;

    @Column(name = "plate_number", nullable = false, unique = true)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(name = "weight_capacity_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightCapacityKg;

    @Column(name = "volume_capacity_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeCapacityM3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Partner getCarrier() { return carrier; }
    public void setCarrier(Partner carrier) { this.carrier = carrier; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    public BigDecimal getWeightCapacityKg() { return weightCapacityKg; }
    public void setWeightCapacityKg(BigDecimal weightCapacityKg) { this.weightCapacityKg = weightCapacityKg; }
    public BigDecimal getVolumeCapacityM3() { return volumeCapacityM3; }
    public void setVolumeCapacityM3(BigDecimal volumeCapacityM3) { this.volumeCapacityM3 = volumeCapacityM3; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
