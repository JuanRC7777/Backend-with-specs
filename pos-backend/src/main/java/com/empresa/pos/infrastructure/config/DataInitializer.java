package com.empresa.pos.infrastructure.config;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaUsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(JpaUsuarioRepository usuarioRepository, 
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Eliminar usuario admin si existe (para recrearlo con el hash correcto)
            usuarioRepository.findByUsername("admin").ifPresent(usuarioRepository::delete);
            
            // Crear usuario admin
            UsuarioEntity admin = UsuarioEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Administrador")
                    .rol("ADMIN")
                    .activo(true)
                    .build();
            
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario admin creado exitosamente");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
        };
    }
}
