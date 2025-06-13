package com.diary.api.domain.chat.service;

import com.diary.api.domain.chat.dto.ChatDto;
import com.diary.api.domain.chat.entity.ChatMessage;
import com.diary.api.domain.chat.entity.ChatSession;
import com.diary.api.domain.chat.repository.ChatMessageRepository;
import com.diary.api.domain.chat.repository.ChatSessionRepository;
import com.diary.api.domain.user.config.OpenAIConfig;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAIConfig openAIConfig;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private static final int MAX_HISTORY_SIZE = 20;

    @Transactional
    public ChatDto.ChatCompletionResponse createChatCompletion(ChatDto.ChatCompletionRequest request) {
        // 단일 메시지를 위한 리스트 생성
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage("user", request.getMessage());
        messages.add(userMessage);

        // AI 응답 생성
        String aiResponse = generateAIResponse(messages);

        // 대화 기록에 사용자 메시지와 AI 응답만 포함
        List<ChatDto.Message> conversationHistory = new ArrayList<>();
        conversationHistory.add(new ChatDto.Message("user", request.getMessage()));
        conversationHistory.add(new ChatDto.Message("assistant", aiResponse));

        return ChatDto.ChatCompletionResponse.builder()
                .response(aiResponse)
                .conversationHistory(conversationHistory)
                .sessionId(null)
                .build();
    }

    @Transactional
    public ChatDto.ChatCompletionResponse createConversation(Long userId, String sessionId,
                                                             boolean newConversation,
                                                             ChatDto.ChatCompletionRequest request) {
        ChatSession session;

        // 새 대화 시작이거나 세션 ID가 없는 경우
        if (newConversation || sessionId == null || sessionId.isEmpty()) {
            session = new ChatSession(userId);
            chatSessionRepository.save(session); // 여기서 먼저 세션 저장
        } else {
            session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("세션을 찾을 수 없습니다"));
        }

        // 사용자 메시지 생성 및 저장
        ChatMessage userMessage = new ChatMessage("user", request.getMessage());
        userMessage.setSession(session);
        chatMessageRepository.save(userMessage); // 명시적으로 메시지 저장
        session.getMessages().add(userMessage);  // messages 컬렉션에 추가

        // AI 응답 생성
        String aiResponse = generateAIResponse(session.getMessages());

        // AI 응답 메시지 생성 및 저장
        ChatMessage assistantMessage = new ChatMessage("assistant", aiResponse);
        assistantMessage.setSession(session);
        chatMessageRepository.save(assistantMessage); // 명시적으로 메시지 저장
        session.getMessages().add(assistantMessage);  // messages 컬렉션에 추가

        // 세션 업데이트
        chatSessionRepository.save(session);

        // 전체 대화 기록 포함하여 반환
        List<ChatDto.Message> conversationHistory = session.getMessages().stream()
                .map(msg -> new ChatDto.Message(msg.getRole(), msg.getContent()))
                .collect(Collectors.toList());

        return ChatDto.ChatCompletionResponse.builder()
                .response(aiResponse)
                .sessionId(session.getId())
                .conversationHistory(conversationHistory)
                .build();
    }

    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자의 세션을 찾을 수 없습니다"));

        chatSessionRepository.delete(session);
    }

    private String generateAIResponse(List<ChatMessage> messages) {
        OpenAiService service = new OpenAiService(openAIConfig.getApiKey());

        List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = messages.stream()
                .map(msg -> new com.theokanning.openai.completion.chat.ChatMessage(msg.getRole(), msg.getContent()))
                .collect(Collectors.toList());

        ChatCompletionRequest openAiRequest = ChatCompletionRequest.builder()
                .model(openAIConfig.getModel())
                .messages(openAiMessages)
                .maxTokens(openAIConfig.getMaxTokens())
                .temperature(openAIConfig.getTemperature())
                .build();

        ChatCompletionResult result = service.createChatCompletion(openAiRequest);
        return result.getChoices().get(0).getMessage().getContent();
    }

    // 30일이 지난 세션 자동 삭제 (선택적 기능)
    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    @Transactional
    public void cleanupOldSessions() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<ChatSession> oldSessions = chatSessionRepository.findByUpdatedAtBefore(thirtyDaysAgo);

        if (!oldSessions.isEmpty()) {
            chatSessionRepository.deleteAll(oldSessions);
        }
    }
}