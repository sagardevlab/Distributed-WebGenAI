package com.sagardevlab.distributed_webgenai.intelligence_service.repository;

import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatSession;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {
}
