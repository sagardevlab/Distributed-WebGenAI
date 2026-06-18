package com.sagardevlab.distributed_webgenai.workspace_service.dto.project;


import com.sagardevlab.distributed_webgenai.common_lib.enums.ProjectRole;

import java.time.Instant;

public record ProjectSummaryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        ProjectRole role
) {
}
