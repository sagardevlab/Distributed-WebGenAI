package com.sagardevlab.distributed_webgenai.workspace_service.dto.project;

public record FileContentResponse(
        String path,
        String content
) {
}
