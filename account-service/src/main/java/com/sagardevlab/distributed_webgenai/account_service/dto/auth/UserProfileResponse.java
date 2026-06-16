package com.sagardevlab.distributed_webgenai.account_service.dto.auth;

public record UserProfileResponse(
        Long id,
        String username,
        String name
) {
}
