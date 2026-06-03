package com.jaiot.controller;

import com.jaiot.entity.ChatMessage;
import com.jaiot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestParam(defaultValue = "device-001") String sessionId) {
        List<ChatMessage> messages = chatMessageRepository
                .findBySessionIdOrderByCreateTimeAsc(sessionId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveMessage(@RequestBody Map<String, String> body) {
        String sessionId = body.getOrDefault("sessionId", "device-001");
        String role = body.get("role");
        String content = body.get("content");

        if (role == null || content == null) {
            return ResponseEntity.badRequest().build();
        }

        ChatMessage msg = ChatMessage.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
        chatMessageRepository.save(msg);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @RequestParam(defaultValue = "device-001") String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
        return ResponseEntity.ok().build();
    }
}
