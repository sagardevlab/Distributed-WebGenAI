package com.sagardevlab.distributed_webgenai.api_gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtGatewayService {

    @Value("${jwt.secret-key:}")
    private String secretKey;

    @PostConstruct
    void validateSecret() {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be set to a random value of at least 32 characters");
        }
    }

    public void validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
