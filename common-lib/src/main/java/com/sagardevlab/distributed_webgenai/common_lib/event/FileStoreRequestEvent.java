package com.sagardevlab.distributed_webgenai.common_lib.event;

public record FileStoreRequestEvent(
        Long projectId,
        String sagaId,
        String filePath,
        String content,
        Long userId
) {}