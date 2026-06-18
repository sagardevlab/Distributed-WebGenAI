package com.sagardevlab.distributed_webgenai.intelligence_service.service;


import com.sagardevlab.distributed_webgenai.intelligence_service.dto.chat.ChatResponse;

import java.util.List;

public interface ChatService {

    List<ChatResponse> getProjectChatHistory(Long projectId);
}
