package com.sagardevlab.distributed_webgenai.account_service.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {

}
