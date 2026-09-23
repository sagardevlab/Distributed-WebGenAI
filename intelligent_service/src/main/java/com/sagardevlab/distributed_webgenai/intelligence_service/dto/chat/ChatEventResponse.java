package com.sagardevlab.distributed_webgenai.intelligence_service.dto.chat;


import com.sagardevlab.distributed_webgenai.common_lib.enums.ChatEventType;

public record ChatEventResponse(
        Long id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata
) {
}
