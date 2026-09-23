package com.sagardevlab.distributed_webgenai.api_gateway.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtGatewayServiceTest {

    private static final String SECRET = "test-secret-0123456789-abcdefghijklmnopqrstuvwxyz";

    private static JwtGatewayService service(String secret) {
        JwtGatewayService service = new JwtGatewayService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        return service;
    }

    private static String token(String secret, Date expiration) {
        return Jwts.builder()
                .subject("ada@example.com")
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void acceptsTokenSignedWithTheSharedSecret() {
        String token = token(SECRET, new Date(System.currentTimeMillis() + 60_000));

        assertThatCode(() -> service(SECRET).validateToken(token)).doesNotThrowAnyException();
    }

    @Test
    void rejectsForeignAndExpiredTokens() {
        String foreign = token("another-secret-0123456789-abcdefghijklmnopqrstuvwxyz", new Date(System.currentTimeMillis() + 60_000));
        String expired = token(SECRET, new Date(System.currentTimeMillis() - 60_000));

        assertThatThrownBy(() -> service(SECRET).validateToken(foreign)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> service(SECRET).validateToken(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    void missingSecretFailsFast() {
        assertThatThrownBy(() -> service("").validateSecret()).isInstanceOf(IllegalStateException.class);
    }
}
