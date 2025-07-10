package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.dto.MessageRequestDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.service.ChatService;
import org.openlake.workSync.app.repo.MessageRepo;
import org.openlake.workSync.app.repo.ChatRepo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.openlake.workSync.app.dto.PagedResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.domain.entity.Chat;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;
    private final MessageRepo messageRepo;
    private final ChatRepo chatRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send/{chatId}")
    public void sendMessage(User user, @Payload MessageRequestDTO message, @DestinationVariable UUID chatId) {
        log.debug("Received message for chat {} from user: {}", chatId, user != null ? user.getUsername() : "null");
        
        try {
            if (user == null) {
                log.error("User not authenticated for chat message");
                throw new RuntimeException("User not authenticated");
            }
            
            log.debug("User ID: {}", user.getId());
            log.debug("User username: {}", user.getUsername());
            log.debug("Sending message to chat service for user: {} in chat: {}", user.getUsername(), chatId);
            chatService.sendMessage(chatId, message, user.getId());
            log.debug("Message sent successfully");
        } catch (Exception ex) {
            log.error("Error sending message to chat {}: {}", chatId, ex.getMessage(), ex);
            
            // Only try to send error message if user is authenticated
            if (user != null) {
                try {
                    String errorTopic = "/user/queue/errors";
                    messagingTemplate.convertAndSendToUser(user.getUsername(), errorTopic, ex.getMessage());
                } catch (Exception errorEx) {
                    log.error("Failed to send error message to user: {}", errorEx.getMessage());
                }
            } else {
                // Send to general error topic if user is not authenticated
                try {
                    messagingTemplate.convertAndSend("/topic/errors", "Authentication failed: " + ex.getMessage());
                } catch (Exception errorEx) {
                    log.error("Failed to send error message to general topic: {}", errorEx.getMessage());
                }
            }
        }
    }

    @GetMapping("/api/chats/{chatId}/messages")
    public PagedResponse<MessageResponseDTO> getChatHistory(@PathVariable UUID chatId, @PageableDefault Pageable pageable) {
        return chatService.getChatHistory(chatId, pageable);
    }

    // Debug endpoint to list all chats
    @GetMapping("/api/chats")
    public List<Chat> getAllChats() {
        List<Chat> chats = chatRepo.findAll();
        log.debug("Found {} chats in database", chats.size());
        for (Chat chat : chats) {
            log.debug("Chat: {} - {} (Project: {})", chat.getId(), chat.getName(), chat.getProject().getId());
        }
        return chats;
    }

    // Debug endpoint to check if a specific chat exists
    @GetMapping("/api/chats/{chatId}/exists")
    public boolean chatExists(@PathVariable UUID chatId) {
        boolean exists = chatRepo.existsById(chatId);
        log.debug("Chat {} exists: {}", chatId, exists);
        return exists;
    }
}
