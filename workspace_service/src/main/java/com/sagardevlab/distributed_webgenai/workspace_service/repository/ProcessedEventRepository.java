package com.sagardevlab.distributed_webgenai.workspace_service.repository;

import com.sagardevlab.distributed_webgenai.workspace_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
