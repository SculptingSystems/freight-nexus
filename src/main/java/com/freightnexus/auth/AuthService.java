package com.freightnexus.auth;

import com.freightnexus.driver.DriverRepository;
import com.freightnexus.partner.PartnerRepository;
import com.freightnexus.partner.PartnerType;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final PartnerRepository partnerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(PartnerRepository partnerRepository,
                       DriverRepository driverRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.partnerRepository = partnerRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public java.util.Optional<AuthDTO.TokenResponse> login(AuthDTO.LoginRequest request) {
        return partnerRepository.findByEmail(request.email())
                .filter(p -> p.getPasswordHash() != null &&
                             passwordEncoder.matches(request.password(), p.getPasswordHash()))
                .map(p -> {
                    UserRole role = p.getType() == PartnerType.CARRIER ? UserRole.CARRIER : UserRole.SHIPPER;
                    return new AuthDTO.TokenResponse(jwtService.generateToken(p.getId(), role), p.getId(), role.name());
                });
    }

    public java.util.Optional<AuthDTO.TokenResponse> driverLogin(AuthDTO.LoginRequest request) {
        return driverRepository.findByEmail(request.email())
                .filter(d -> d.getPasswordHash() != null &&
                             passwordEncoder.matches(request.password(), d.getPasswordHash()))
                .map(d -> new AuthDTO.TokenResponse(
                        jwtService.generateToken(d.getId(), UserRole.DRIVER), d.getId(), "DRIVER"));
    }
}
