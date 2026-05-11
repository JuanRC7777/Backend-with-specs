package com.empresa.pos.infrastructure.config;

import com.empresa.pos.domain.service.GeneradorNumeroFactura;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class BeanConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public GeneradorNumeroFactura generadorNumeroFactura() {
        return new GeneradorNumeroFactura();
    }
}
