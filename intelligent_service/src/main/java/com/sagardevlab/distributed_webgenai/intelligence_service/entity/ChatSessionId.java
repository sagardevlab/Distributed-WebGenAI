package com.sagardevlab.distributed_webgenai.intelligence_service.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class ChatSessionId implements Serializable {
    Long projectId;
    Long userId;
}
