package com.sagardevlab.distributed_webgenai.workspace_service.dto.member;

import com.sagardevlab.distributed_webgenai.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull ProjectRole role) {
}
