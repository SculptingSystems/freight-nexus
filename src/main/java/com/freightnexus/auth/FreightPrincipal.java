package com.freightnexus.auth;

// Stored in the SecurityContext after JWT validation.
// id = partnerId for CARRIER/SHIPPER, driverId for DRIVER.
public record FreightPrincipal(Long id, UserRole role) {}
