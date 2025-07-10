package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.MessageRequestDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.service.ChatService;
import org.openlake.workSync.app.repo.MessageRepo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.openlake.workSync.app.dto.PagedResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MessageRepo messageRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send/{chatId}")
    public void sendMessage(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @Payload MessageRequestDTO message, @PathVariable UUID chatId) {
        try {
            chatService.sendMessage(chatId, message, user.getId());
        } catch (Exception ex) {
            String errorTopic = "/user/queue/errors";
            messagingTemplate.convertAndSendToUser(user.getUsername(), errorTopic, ex.getMessage());
        }
    }

    @GetMapping("/api/chats/{chatId}/messages")
    public PagedResponse<MessageResponseDTO> getChatHistory(@PathVariable UUID chatId, @PageableDefault Pageable pageable) {
        return chatService.getChatHistory(chatId, pageable);
    }
}
