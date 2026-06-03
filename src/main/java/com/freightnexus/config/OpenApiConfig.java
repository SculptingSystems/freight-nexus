package com.freightnexus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Freight Nexus API")
                        .description("""
                                B2B freight distribution platform connecting carriers with shippers.

                                **Three user roles:**
                                - **CARRIER** — registers vehicles, drivers, lanes, rate plans, sets capacity, manages contracts
                                - **SHIPPER** — creates shipments, books loads, tracks deliveries
                                - **DRIVER** — posts GPS tracking events, updates load status

                                Authenticate via `POST /auth/login` (carrier/shipper) or `POST /auth/driver-login` (driver) to get a Bearer JWT token.
                                """)
                        .version("1.0.0")
                        .license(new License().name("MIT")
                                .url("https://github.com/SculptingSystems/freight-nexus/blob/main/LICENSE")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
