package com.diary.api.controller;

import com.diary.api.domain.chat.dto.ChatDto.ChatCompletionRequest;
import com.diary.api.domain.chat.dto.ChatDto.ChatCompletionResponse;
import com.diary.api.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/completions")
    public ResponseEntity<ChatCompletionResponse> chatCompletion(
            @RequestBody ChatCompletionRequest request) {
        ChatCompletionResponse response = chatService.createChatCompletion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversation")
    public ResponseEntity<ChatCompletionResponse> conversation(
            @RequestBody ChatCompletionRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "false") boolean newConversation,
            @RequestParam Long userId) { // AuthenticationPrincipal 대신 직접 파라미터로 받기

        try {
            ChatCompletionResponse response = chatService.createConversation(userId, sessionId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 로그에 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatCompletionResponse.builder()
                            .response("오류 발생: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/conversation/{sessionId}")
    public ResponseEntity<?> deleteConversation(
            @PathVariable String sessionId,
            @RequestParam Long userId) { // AuthenticationPrincipal 대신 직접 파라미터로 받기

        try {
            chatService.deleteSession(userId, sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("세션 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}