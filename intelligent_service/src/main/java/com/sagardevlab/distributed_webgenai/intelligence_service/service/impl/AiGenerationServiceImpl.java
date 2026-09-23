package com.sagardevlab.distributed_webgenai.intelligence_service.service.impl;

import com.sagardevlab.distributed_webgenai.common_lib.enums.ChatEventStatus;
import com.sagardevlab.distributed_webgenai.common_lib.enums.ChatEventType;
import com.sagardevlab.distributed_webgenai.common_lib.enums.MessageRole;
import com.sagardevlab.distributed_webgenai.common_lib.error.ResourceNotFoundException;
import com.sagardevlab.distributed_webgenai.common_lib.event.FileStoreRequestEvent;
import com.sagardevlab.distributed_webgenai.common_lib.security.AuthUtil;
import com.sagardevlab.distributed_webgenai.intelligence_service.client.WorkspaceClient;
import com.sagardevlab.distributed_webgenai.intelligence_service.dto.chat.StreamResponse;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatEvent;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatMessage;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatSession;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatSessionId;
import com.sagardevlab.distributed_webgenai.intelligence_service.llm.CodeGenerationTools;
import com.sagardevlab.distributed_webgenai.intelligence_service.llm.FileTreeContextAdvisor;
import com.sagardevlab.distributed_webgenai.intelligence_service.llm.LlmResponseParser;
import com.sagardevlab.distributed_webgenai.intelligence_service.llm.PromptUtils;
import com.sagardevlab.distributed_webgenai.intelligence_service.repository.ChatEventRepository;
import com.sagardevlab.distributed_webgenai.intelligence_service.repository.ChatMessageRepository;
import com.sagardevlab.distributed_webgenai.intelligence_service.repository.ChatSessionRepository;
import com.sagardevlab.distributed_webgenai.intelligence_service.service.AiGenerationService;
import com.sagardevlab.distributed_webgenai.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatSessionRepository chatSessionRepository;
    private final LlmResponseParser llmResponseParser;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatEventRepository chatEventRepository;
    private final UsageService usageService;
    private final WorkspaceClient workspaceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> streamResponse(String userMessage, Long projectId) {

        usageService.checkDailyTokensUsage();

        Long userId = authUtil.getCurrentUserId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);

        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId,
                FileTreeContextAdvisor.AUTHENTICATION_PARAM, authentication
        );

        StringBuilder fullResponseBuffer = new StringBuilder();
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectId, workspaceClient, authentication);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTools)
                .advisors(advisorSpec -> {
                            advisorSpec.params(advisorParams);
                            advisorSpec.advisors(fileTreeContextAdvisor);
                        }
                )
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    if (response.getResults() != null && !response.getResults().isEmpty()) {
                        String content = response.getResult().getOutput().getText();

                        if(content != null && !content.isEmpty() && endTime.get() == 0) { // first non-empty chunk received
                            endTime.set(System.currentTimeMillis());
                        }
                        if(response.getMetadata().getUsage() != null) {
                            usageRef.set(response.getMetadata().getUsage());
                        }
                        fullResponseBuffer.append(content);
                    }

                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
//                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);

                        long duration = (endTime.get() - startTime.get()) /  1000;
                        finalizeChats(userMessage, chatSession, fullResponseBuffer.toString(), duration, usageRef.get(), userId);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId, error))
                .map(response -> {
                    if (response.getResults() != null && !response.getResults().isEmpty()) {
                        String text = response.getResult().getOutput().getText();
                        return new StreamResponse(text != null ? text : "");
                    }
                    return new StreamResponse("");
                })
                // Surface AI provider failures in the chat instead of a bare HTTP 500
                .onErrorResume(error -> Flux.just(new StreamResponse(
                        "<message phase=\"error\">" + describeAiError(error) + "</message>")));
    }

    private static final Pattern PROVIDER_STATUS = Pattern.compile("Status: \\[(\\d{3})");

    private String describeAiError(Throwable error) {
        Matcher matcher = PROVIDER_STATUS.matcher(String.valueOf(error.getMessage()));
        String status = matcher.find() ? matcher.group(1) : "";
        return switch (status) {
            case "401", "403" -> "The AI provider rejected the API key. Set a valid **ANTHROPIC_API_KEY** and restart the intelligence service.";
            case "429" -> "The AI provider is rate limiting requests. Wait a moment and try again.";
            case "529", "500", "502", "503" -> "The AI provider is temporarily unavailable. Please try again shortly.";
            case "" -> "Generation failed: " + error.getClass().getSimpleName() + ". Check the intelligence service logs.";
            default -> "The AI provider returned an error (HTTP " + status + "). Check the intelligence service logs.";
        };
    }

    private void finalizeChats(String userMessage, ChatSession chatSession, String fullText, Long duration, Usage usage, Long userId) {
        Long projectId = chatSession.getId().getProjectId();

        if(usage != null) {
            int totalTokens = usage.getTotalTokens();
            usageService.recordTokenUsage(chatSession.getId().getUserId(), totalTokens);
        }

        // Save the User message
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .tokensUsed(usage != null ? usage.getPromptTokens() : 0)
                        .build()
        );

        ChatMessage assistantChatMessage = ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content("Assistant Message here...")
                .chatSession(chatSession)
                .tokensUsed(usage != null ? usage.getCompletionTokens() : 0)
                .build();

        assistantChatMessage = chatMessageRepository.save(assistantChatMessage);

        List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText, assistantChatMessage);
        chatEventList.addFirst(ChatEvent.builder()
                        .type(ChatEventType.THOUGHT)
                        .status(ChatEventStatus.CONFIRMED)
                        .chatMessage(assistantChatMessage)
                        .content("Thought for "+duration+"s")
                        .sequenceOrder(0)
                .build());

        List<ChatEvent> fileEdits = chatEventList.stream()
                .filter(e -> e.getType() == ChatEventType.FILE_EDIT)
                .toList();
        fileEdits.forEach(e -> e.setSagaId(UUID.randomUUID().toString()));

        chatEventRepository.saveAll(chatEventList);

        fileEdits.forEach(e -> {
            FileStoreRequestEvent fileStoreRequestEvent = new FileStoreRequestEvent(
                    projectId,
                    e.getSagaId(),
                    e.getFilePath(),
                    e.getContent(),
                    userId
            );
            log.info("Storage request event sent: {}", e.getFilePath());
            kafkaTemplate.send("file-storage-request-event", "project-"+projectId, fileStoreRequestEvent);
        });
    }

    private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);

        if(chatSession == null) {
            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .build();

            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;
    }
}