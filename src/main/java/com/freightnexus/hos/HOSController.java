package com.freightnexus.hos;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.driver.Driver;
import com.freightnexus.driver.DriverRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drivers")
public class HOSController {

    private final HOSService hosService;
    private final DriverRepository driverRepository;

    public HOSController(HOSService hosService, DriverRepository driverRepository) {
        this.hosService = hosService;
        this.driverRepository = driverRepository;
    }

    @GetMapping("/{id}/hos-status")
    public HOSService.HOSStatusDTO hosStatus(@PathVariable Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        return hosService.getStatus(driver);
    }
}
