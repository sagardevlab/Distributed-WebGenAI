package com.sagardevlab.distributed_webgenai.workspace_service.service;

import com.sagardevlab.distributed_webgenai.workspace_service.dto.project.DeployResponse;
import org.jspecify.annotations.Nullable;

public interface DeploymentService {
    @Nullable DeployResponse deploy(Long projectId);
}
