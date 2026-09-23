package com.sagardevlab.distributed_webgenai.common_lib.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUtilTest {

    private static final String SECRET = "test-secret-0123456789-abcdefghijklmnopqrstuvwxyz";

    private static AuthUtil authUtil(String secret) {
        AuthUtil authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "jwtSecretKey", secret);
        return authUtil;
    }

    @Test
    void generatedTokenVerifiesBackToTheSameUser() {
        AuthUtil authUtil = authUtil(SECRET);
        String token = authUtil.generateAccessToken(
                new JwtUserPrincipal(42L, "Ada", "ada@example.com", null, new ArrayList<>()));

        JwtUserPrincipal principal = authUtil.verifyAccessToken(token);

        assertThat(principal.userId()).isEqualTo(42L);
        assertThat(principal.name()).isEqualTo("Ada");
        assertThat(principal.username()).isEqualTo("ada@example.com");
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = authUtil("another-secret-0123456789-abcdefghijklmnopqrstuvwxyz").generateAccessToken(
                new JwtUserPrincipal(1L, "Eve", "eve@example.com", null, new ArrayList<>()));

        assertThatThrownBy(() -> authUtil(SECRET).verifyAccessToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void missingOrShortSecretFailsFast() {
        assertThatThrownBy(() -> authUtil("").validateSecret()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> authUtil("too-short").validateSecret()).isInstanceOf(IllegalStateException.class);
    }
}
