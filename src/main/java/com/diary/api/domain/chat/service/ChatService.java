package com.diary.api.domain.chat.service;

import com.diary.api.domain.chat.dto.ChatDto;
import com.diary.api.domain.chat.entity.ChatMessage;
import com.diary.api.domain.chat.entity.ChatSession;
import com.diary.api.domain.chat.repository.ChatSessionRepository;
import com.diary.api.domain.user.config.OpenAIConfig;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAIConfig openAIConfig;
    private final ChatSessionRepository chatSessionRepository;
    private static final int MAX_HISTORY_SIZE = 20;

    @Transactional
    public ChatDto.ChatCompletionResponse createChatCompletion(ChatDto.ChatCompletionRequest request) {
        OpenAiService service = new OpenAiService(openAIConfig.getApiKey());

        List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
        messages.add(new com.theokanning.openai.completion.chat.ChatMessage("user", request.getMessage()));

        ChatCompletionRequest openAiRequest = ChatCompletionRequest.builder()
                .model(openAIConfig.getModel())
                .messages(messages)
                .maxTokens(openAIConfig.getMaxTokens())
                .temperature(openAIConfig.getTemperature())
                .build();

        ChatCompletionResult result = service.createChatCompletion(openAiRequest);
        String responseContent = result.getChoices().get(0).getMessage().getContent();

        return ChatDto.ChatCompletionResponse.builder()
                .response(responseContent)
                .build();
    }

    @Transactional
    public ChatDto.ChatCompletionResponse createConversation(Long userId, String sessionId, ChatDto.ChatCompletionRequest request) {
        ChatSession session;

        // 세션이 없거나 새 대화 요청이면 새 세션 생성
        if (sessionId == null || sessionId.isEmpty()) {
            session = new ChatSession(userId);
            chatSessionRepository.save(session);
        } else {
            session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                    .orElseGet(() -> {
                        ChatSession newSession = new ChatSession(userId);
                        chatSessionRepository.save(newSession);
                        return newSession;
                    });
        }

        // 메시지 수 제한 (최대 40개 = 20쌍)
        if (session.getMessages().size() >= MAX_HISTORY_SIZE * 2) {
            List<ChatMessage> messages = session.getMessages();
            // 가장 오래된 메시지 2개 제거 (사용자+봇 한 쌍)
            messages.remove(0);
            if (!messages.isEmpty()) {
                messages.remove(0);
            }
        }

        // 사용자 메시지 추가
        ChatMessage userMessage = new ChatMessage("user", request.getMessage());
        session.addMessage(userMessage);

        // API 요청 준비
        List<com.theokanning.openai.completion.chat.ChatMessage> apiMessages = session.getMessages().stream()
                .map(msg -> new com.theokanning.openai.completion.chat.ChatMessage(msg.getRole(), msg.getContent()))
                .collect(Collectors.toList());

        // API 호출
        OpenAiService service = new OpenAiService(openAIConfig.getApiKey());

        ChatCompletionRequest openAiRequest = ChatCompletionRequest.builder()
                .model(openAIConfig.getModel())
                .messages(apiMessages)
                .maxTokens(openAIConfig.getMaxTokens())
                .temperature(openAIConfig.getTemperature())
                .build();

        ChatCompletionResult result = service.createChatCompletion(openAiRequest);
        String responseContent = result.getChoices().get(0).getMessage().getContent();

        // 응답 메시지 추가
        ChatMessage assistantMessage = new ChatMessage("assistant", responseContent);
        session.addMessage(assistantMessage);
        chatSessionRepository.save(session);

        // DTO로 변환
        List<ChatDto.Message> historyDto = session.getMessages().stream()
                .map(msg -> ChatDto.Message.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList());

        return ChatDto.ChatCompletionResponse.builder()
                .response(responseContent)
                .conversationHistory(historyDto)
                .build();
    }

    @Transactional
    public void clearConversationHistory(Long userId, String sessionId) {
        chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .ifPresent(session -> {
                    session.clearMessages();
                    chatSessionRepository.save(session);
                });
    }

    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .ifPresent(chatSessionRepository::delete);
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void cleanupOldSessions() {
        // 30일 이상 사용하지 않은 세션 삭제
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
        List<ChatSession> oldSessions = chatSessionRepository.findByUpdatedAtBefore(cutoffTime);
        chatSessionRepository.deleteAll(oldSessions);
    }
}