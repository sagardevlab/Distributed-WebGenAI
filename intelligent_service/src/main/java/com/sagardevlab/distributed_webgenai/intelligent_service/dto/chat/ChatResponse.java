package com.sagardevlab.distributed_webgenai.intelligence_service.dto.chat;


import com.sagardevlab.distributed_webgenai.common_lib.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        MessageRole role,
        List<ChatEventResponse> events,
        String content,
        Integer tokensUsed,
        Instant createdAt

) {
}
