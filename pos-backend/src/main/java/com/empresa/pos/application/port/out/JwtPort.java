package com.empresa.pos.application.port.out;

public interface JwtPort {

    String generateToken(String username);

    boolean validateToken(String token);

    String extractUsername(String token);
}
