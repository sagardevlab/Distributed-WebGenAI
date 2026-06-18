package com.sagardevlab.distributed_webgenai.workspace_service.dto.member;


import com.sagardevlab.distributed_webgenai.common_lib.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String username,
        String name,
        ProjectRole projectRole,
        Instant invitedAt
) {
}
