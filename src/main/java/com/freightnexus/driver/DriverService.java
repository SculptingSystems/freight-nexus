package com.freightnexus.driver;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.ForbiddenException;
import com.freightnexus.common.PageResponse;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.partner.Partner;
import com.freightnexus.partner.PartnerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    public DriverService(DriverRepository driverRepository, PartnerRepository partnerRepository,
                         PasswordEncoder passwordEncoder) {
        this.driverRepository = driverRepository;
        this.partnerRepository = partnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<DriverDTO.Response> findMyDrivers(Pageable pageable) {
        return PageResponse.from(
                driverRepository.findByCarrier_Id(caller().id(), pageable).map(this::toResponse));
    }

    public DriverDTO.Response findById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        if (!driver.getCarrier().getId().equals(caller().id())) {
            throw new ForbiddenException("Driver belongs to a different carrier");
        }
        return toResponse(driver);
    }

    @Transactional
    public DriverDTO.Response create(DriverDTO.Request request) {
        Partner carrier = partnerRepository.findById(caller().id())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found"));
        Driver driver = new Driver();
        driver.setCarrier(carrier);
        driver.setName(request.name());
        driver.setEmail(request.email());
        driver.setPasswordHash(passwordEncoder.encode(request.password()));
        driver.setLicenseNumber(request.licenseNumber());
        driver.setLicenseExpiry(request.licenseExpiry());
        return toResponse(driverRepository.save(driver));
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private DriverDTO.Response toResponse(Driver d) {
        return new DriverDTO.Response(d.getId(), d.getCarrier().getId(), d.getCarrier().getName(),
                d.getName(), d.getEmail(), d.getLicenseNumber(),
                d.getLicenseExpiry(), d.getStatus(), d.getCreatedAt());
    }
}
