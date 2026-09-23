package com.sagardevlab.distributed_webgenai.workspace_service.service;

import com.sagardevlab.distributed_webgenai.workspace_service.dto.project.DeployResponse;
import org.jspecify.annotations.Nullable;

public interface DeploymentService {
    @Nullable DeployResponse deploy(Long projectId);

    /** Called after a file of the project was persisted, so a running preview can pick it up. */
    default void onFileSaved(Long projectId, String path, String content) {
    }
}
