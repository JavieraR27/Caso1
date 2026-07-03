package com.example.administrador.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Emisión y validación de JWT (HS256, secreto compartido entre servicios).
 * Roles: CLIENTE, PROVEEDOR, ADMINISTRADOR e INTERNO (servicio a servicio).
 */
@Component
public class JwtUtil {

    private static final long EXPIRACION_MS = 8 * 60 * 60 * 1000; // 8 horas

    private final SecretKey key;

    public JwtUtil(@Value("${paris.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generar(String subject, String rol) {
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(key)
                .compact();
    }

    /** Token de servicio para las llamadas WebClient entre microservicios. */
    public String generarInterno(String servicio) {
        return generar(servicio, "INTERNO");
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
