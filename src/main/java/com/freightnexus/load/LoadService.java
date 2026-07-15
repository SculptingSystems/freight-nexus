package com.freightnexus.load;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.auth.UserRole;
import com.freightnexus.capacity.VehicleCapacity;
import com.freightnexus.capacity.VehicleCapacityRepository;
import com.freightnexus.common.ForbiddenException;
import com.freightnexus.common.InsufficientCapacityException;
import com.freightnexus.common.PageResponse;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.contract.ContractStatus;
import com.freightnexus.contract.ShipperContractRepository;
import com.freightnexus.driver.Driver;
import com.freightnexus.driver.DriverRepository;
import com.freightnexus.hos.HOSService;
import com.freightnexus.rateplan.PricingModel;
import com.freightnexus.rateplan.RatePlan;
import com.freightnexus.rateplan.RatePlanRepository;
import com.freightnexus.shipment.Shipment;
import com.freightnexus.shipment.ShipmentRepository;
import com.freightnexus.shipment.ShipmentStatus;
import com.freightnexus.vehicle.Vehicle;
import com.freightnexus.vehicle.VehicleRepository;
import com.freightnexus.webhook.WebhookOutboxService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LoadService {

    private final LoadRepository loadRepository;
    private final LoadStatusHistoryRepository historyRepository;
    private final ShipmentRepository shipmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RatePlanRepository ratePlanRepository;
    private final VehicleCapacityRepository capacityRepository;
    private final ShipperContractRepository contractRepository;
    private final HOSService hosService;
    private final WebhookOutboxService webhookOutboxService;
    private final MeterRegistry meterRegistry;

    public LoadService(LoadRepository loadRepository,
                       LoadStatusHistoryRepository historyRepository,
                       ShipmentRepository shipmentRepository,
                       VehicleRepository vehicleRepository,
                       DriverRepository driverRepository,
                       RatePlanRepository ratePlanRepository,
                       VehicleCapacityRepository capacityRepository,
                       ShipperContractRepository contractRepository,
                       HOSService hosService,
                       WebhookOutboxService webhookOutboxService,
                       MeterRegistry meterRegistry) {
        this.loadRepository = loadRepository;
        this.historyRepository = historyRepository;
        this.shipmentRepository = shipmentRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.ratePlanRepository = ratePlanRepository;
        this.capacityRepository = capacityRepository;
        this.contractRepository = contractRepository;
        this.hosService = hosService;
        this.webhookOutboxService = webhookOutboxService;
        this.meterRegistry = meterRegistry;
    }

    public LoadDTO.Response findById(Long id) {
        return toResponse(loadRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found: " + id)));
    }

    public PageResponse<LoadDTO.Response> findMine(Pageable pageable) {
        FreightPrincipal me = caller();
        if (me.role() == UserRole.SHIPPER) {
            return PageResponse.from(loadRepository.findByShipment_Shipper_Id(me.id(), pageable).map(this::toResponse));
        }
        return PageResponse.from(loadRepository.findByVehicle_Carrier_Id(me.id(), pageable).map(this::toResponse));
    }

    public List<LoadDTO.HistoryEntry> findHistory(Long id) {
        return historyRepository.findByLoad_IdOrderByOccurredAtAsc(id).stream().map(h ->
                new LoadDTO.HistoryEntry(h.getFromStatus(), h.getToStatus(),
                        h.getActorType(), h.getActorId(), h.getNote(), h.getOccurredAt())).toList();
    }

    @Transactional
    public LoadDTO.Response create(LoadDTO.Request request) {
        FreightPrincipal me = caller();

        Shipment shipment = shipmentRepository.findById(request.shipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + request.shipmentId()));
        if (!shipment.getShipper().getId().equals(me.id())) {
            throw new ForbiddenException("Shipment belongs to a different shipper");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));

        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.driverId()));

        RatePlan ratePlan = ratePlanRepository.findById(request.ratePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Rate plan not found: " + request.ratePlanId()));

        // Contract gate: shipper must have active contract with carrier
        Long carrierId = vehicle.getCarrier().getId();
        if (!contractRepository.existsByCarrier_IdAndShipper_IdAndStatus(carrierId, me.id(), ContractStatus.ACTIVE)) {
            throw new ForbiddenException("No active contract with this carrier. Contact them to establish a contract.");
        }

        // HOS compliance check
        int estimatedDrivingMinutes = ratePlan.getLane().getEstimatedHours().multiply(BigDecimal.valueOf(60)).intValue();
        hosService.validateBeforeAssignment(driver, estimatedDrivingMinutes);

        // Pessimistic lock on vehicle capacity
        VehicleCapacity capacity = capacityRepository.lockForBooking(vehicle.getId(), request.pickupDate())
                .orElseThrow(() -> new InsufficientCapacityException(
                        "No capacity record for vehicle " + vehicle.getPlateNumber() + " on " + request.pickupDate()
                        + ". Carrier must set capacity first."));

        if (capacity.availableWeightKg().compareTo(shipment.getWeightKg()) < 0) {
            throw new InsufficientCapacityException(
                    "Insufficient weight capacity: available " + capacity.availableWeightKg()
                    + " kg, required " + shipment.getWeightKg() + " kg.");
        }
        if (capacity.availableVolumeM3().compareTo(shipment.getVolumeM3()) < 0) {
            throw new InsufficientCapacityException(
                    "Insufficient volume capacity: available " + capacity.availableVolumeM3()
                    + " m³, required " + shipment.getVolumeM3() + " m³.");
        }

        // Deduct capacity
        capacity.setBookedWeightKg(capacity.getBookedWeightKg().add(shipment.getWeightKg()));
        capacity.setBookedVolumeM3(capacity.getBookedVolumeM3().add(shipment.getVolumeM3()));
        capacityRepository.save(capacity);

        // Calculate charge based on pricing model
        BigDecimal charge = calculateCharge(ratePlan, shipment);

        Load load = new Load();
        load.setShipment(shipment);
        load.setVehicle(vehicle);
        load.setDriver(driver);
        load.setRatePlan(ratePlan);
        load.setPickupDate(request.pickupDate());
        load.setTotalCharge(charge);
        load.setReference("LD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        Load saved = loadRepository.save(load);

        shipment.setStatus(ShipmentStatus.ASSIGNED);
        shipmentRepository.save(shipment);

        // Record status history
        recordHistory(saved, null, LoadStatus.PENDING, "SHIPPER", me.id(), "Load created");

        meterRegistry.counter("freight.loads", "event", "created").increment();
        webhookOutboxService.enqueue(shipment.getShipper(), "LOAD_CREATED",
                toResponse(loadRepository.findByIdWithDetails(saved.getId()).orElseThrow()));
        return toResponse(loadRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    @Transactional
    public LoadDTO.Response updateStatus(Long id, LoadDTO.StatusUpdateRequest request) {
        FreightPrincipal me = caller();
        Load load = loadRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found: " + id));

        validateStatusTransition(load.getStatus(), request.status(), me.role());
        LoadStatus previous = load.getStatus();
        load.setStatus(request.status());

        if (request.status() == LoadStatus.PICKED_UP || request.status() == LoadStatus.IN_TRANSIT) {
            load.getShipment().setStatus(ShipmentStatus.IN_TRANSIT);
            shipmentRepository.save(load.getShipment());
        }

        if (request.status() == LoadStatus.DELIVERED) {
            load.getShipment().setStatus(ShipmentStatus.DELIVERED);
            shipmentRepository.save(load.getShipment());
            hosService.recordDrivingMinutes(load.getDriver(),
                    load.getRatePlan().getLane().getEstimatedHours().multiply(BigDecimal.valueOf(60)).intValue());
            meterRegistry.counter("freight.loads", "event", "delivered").increment();
        }

        if (request.status() == LoadStatus.FAILED) {
            load.getShipment().setStatus(ShipmentStatus.PENDING);
            shipmentRepository.save(load.getShipment());
            // Restore capacity
            capacityRepository.lockForBooking(load.getVehicle().getId(), load.getPickupDate())
                    .ifPresent(vc -> {
                        vc.setBookedWeightKg(vc.getBookedWeightKg().subtract(load.getShipment().getWeightKg()));
                        vc.setBookedVolumeM3(vc.getBookedVolumeM3().subtract(load.getShipment().getVolumeM3()));
                        capacityRepository.save(vc);
                    });
        }

        Load updated = loadRepository.save(load);
        recordHistory(updated, previous, request.status(), me.role().name(), me.id(), request.note());

        webhookOutboxService.enqueue(load.getShipment().getShipper(), "LOAD_STATUS_CHANGED", toResponse(updated));
        return toResponse(updated);
    }

    private void validateStatusTransition(LoadStatus current, LoadStatus next, UserRole role) {
        boolean valid = switch (current) {
            case PENDING    -> next == LoadStatus.ASSIGNED;
            case ASSIGNED   -> next == LoadStatus.PICKED_UP || next == LoadStatus.FAILED;
            case PICKED_UP  -> next == LoadStatus.IN_TRANSIT || next == LoadStatus.FAILED;
            case IN_TRANSIT -> next == LoadStatus.DELIVERED || next == LoadStatus.FAILED;
            case DELIVERED, FAILED -> false;
        };
        if (!valid) throw new IllegalStateException("Cannot transition load from " + current + " to " + next);

        if ((next == LoadStatus.PICKED_UP || next == LoadStatus.IN_TRANSIT || next == LoadStatus.DELIVERED)
                && role != UserRole.DRIVER) {
            throw new ForbiddenException("Only drivers can mark a load as " + next);
        }
        if (next == LoadStatus.ASSIGNED && role != UserRole.CARRIER) {
            throw new ForbiddenException("Only carriers can assign loads");
        }
    }

    private void recordHistory(Load load, LoadStatus from, LoadStatus to, String actorType, Long actorId, String note) {
        LoadStatusHistory h = new LoadStatusHistory();
        h.setLoad(load);
        h.setFromStatus(from != null ? from.name() : null);
        h.setToStatus(to.name());
        h.setActorType(actorType);
        h.setActorId(actorId);
        h.setNote(note);
        historyRepository.save(h);
    }

    private BigDecimal calculateCharge(RatePlan ratePlan, Shipment shipment) {
        return switch (ratePlan.getPricingModel()) {
            case FLAT   -> ratePlan.getBaseRate();
            case PER_KG -> ratePlan.getBaseRate().multiply(shipment.getWeightKg());
            case PER_KM -> ratePlan.getBaseRate().multiply(ratePlan.getLane().getDistanceKm());
        };
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private LoadDTO.Response toResponse(Load l) {
        return new LoadDTO.Response(
                l.getId(), l.getReference(),
                l.getShipment().getId(), l.getShipment().getShipper().getName(),
                l.getVehicle().getId(), l.getVehicle().getPlateNumber(),
                l.getDriver().getId(), l.getDriver().getName(),
                l.getRatePlan().getId(),
                l.getRatePlan().getLane().getOriginCity(),
                l.getRatePlan().getLane().getDestinationCity(),
                l.getPickupDate(), l.getTotalCharge(), l.getStatus(), l.getCreatedAt());
    }
}
