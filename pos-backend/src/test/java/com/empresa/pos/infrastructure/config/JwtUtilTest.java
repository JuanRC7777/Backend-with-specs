package com.empresa.pos.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para JwtUtil.
 * No levanta contexto de Spring — instancia directamente.
 *
 * @version 3.2.0
 */
@DisplayName("JwtUtil - Tests Unitarios")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Secret de al menos 32 chars para HMAC-SHA256
        String secret = "miClaveSecretaSuperSeguraParaJWTPOS2024";
        long expiration = 86400000L; // 24 horas
        jwtUtil = new JwtUtil(secret, expiration);
    }

    @Test
    @DisplayName("7.6: generateToken debe retornar token válido")
    void generateToken_debeRetornarTokenValido() {
        String token = jwtUtil.generateToken("admin");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("7.6: validateToken debe retornar false cuando token expirado")
    void validateToken_debeRetornarFalse_cuandoTokenExpirado() {
        // Crear JwtUtil con expiración de 1ms
        JwtUtil jwtUtilExpired = new JwtUtil("miClaveSecretaSuperSeguraParaJWTPOS2024", 1L);
        String token = jwtUtilExpired.generateToken("admin");

        // Esperar a que expire
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertThat(jwtUtilExpired.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("7.6: extractUsername debe retornar username correctamente")
    void extractUsername_debeRetornarUsernameCorrectamente() {
        String token = jwtUtil.generateToken("cajero1");

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("cajero1");
    }

    @Test
    @DisplayName("validateToken debe retornar true para token válido")
    void validateToken_debeRetornarTrue_paraTokenValido() {
        String token = jwtUtil.generateToken("admin");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken debe retornar false para token malformado")
    void validateToken_debeRetornarFalse_paraTokenMalformado() {
        assertThat(jwtUtil.validateToken("token.invalido.aqui")).isFalse();
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("extractUsername debe retornar null para token inválido")
    void extractUsername_debeRetornarNull_paraTokenInvalido() {
        assertThat(jwtUtil.extractUsername("token.invalido")).isNull();
    }
}
