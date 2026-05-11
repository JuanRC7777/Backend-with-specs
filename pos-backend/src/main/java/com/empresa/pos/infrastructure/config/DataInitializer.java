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
            var adminOpt = usuarioRepository.findByUsername("admin");

            if (adminOpt.isEmpty()) {
                // Crear usuario admin por primera vez
                UsuarioEntity admin = UsuarioEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .nombre("Administrador")
                        .rol("ADMIN")
                        .activo(true)
                        .build();
                usuarioRepository.save(admin);
                System.out.println("✅ Usuario admin creado exitosamente");
            } else {
                // Actualizar password con BCrypt fresco (por si venía de data.sql con hash fijo)
                UsuarioEntity admin = adminOpt.get();
                if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    usuarioRepository.save(admin);
                    System.out.println("✅ Password de admin actualizado con BCrypt");
                } else {
                    System.out.println("ℹ️  Usuario admin ya existe y password es válido");
                }
            }
            System.out.println("   Username: admin  |  Password: admin123");
        };
    }
}
