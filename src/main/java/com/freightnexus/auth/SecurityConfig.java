package com.freightnexus.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/driver-login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/partners").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Carrier-only
                        .requestMatchers(HttpMethod.POST, "/vehicles").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.POST, "/drivers").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.POST, "/lanes").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.POST, "/lanes/*/rate-plans").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.PUT, "/vehicles/*/capacity").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.POST, "/contracts").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.PUT, "/contracts/*/activate").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.PUT, "/contracts/*/terminate").hasRole("CARRIER")
                        // Shipper-only
                        .requestMatchers(HttpMethod.POST, "/shipments").hasRole("SHIPPER")
                        .requestMatchers(HttpMethod.POST, "/loads").hasRole("SHIPPER")
                        .requestMatchers(HttpMethod.GET, "/loads").hasRole("SHIPPER")
                        // Driver-only
                        .requestMatchers(HttpMethod.POST, "/loads/*/tracking").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/loads/*/status").hasRole("DRIVER")
                        // Any authenticated
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui.html")
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
