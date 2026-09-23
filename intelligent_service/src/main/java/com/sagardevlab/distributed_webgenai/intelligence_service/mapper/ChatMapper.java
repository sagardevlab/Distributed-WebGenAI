package com.sagardevlab.distributed_webgenai.intelligence_service.mapper;

import com.sagardevlab.distributed_webgenai.intelligence_service.dto.chat.ChatResponse;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessageList);
}
