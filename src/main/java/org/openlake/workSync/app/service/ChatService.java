package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.dto.ChatEventDTO;
import org.openlake.workSync.app.dto.MessageRequestDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.kafka.ChatEventProducer;
import org.openlake.workSync.app.mapper.MessageMapper;
import org.openlake.workSync.app.repo.ChatRepo;
import org.openlake.workSync.app.repo.MessageRepo;
import org.openlake.workSync.app.repo.UserRepo;
import org.openlake.workSync.app.domain.exception.ResourceNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatEventProducer chatEventProducer;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;

    public void sendMessage(UUID chatId, MessageRequestDTO request, UUID senderId) {
        var sender = userRepo.findById(senderId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(senderId));
        
        UUID eventId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        
        var response = MessageResponseDTO.builder()
            .chatId(chatId)
            .senderId(senderId)
            .senderUsername(sender.getUsername())
            .senderName(sender.getName())
            .senderAvatarUrl(sender.getAvatarUrl())
            .content(request.getContent())
            .sentAt(timestamp)
            .build();
        
        try {
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, response);
            log.debug("Broadcasted message to WebSocket subscribers for chat {}", chatId);
        } catch (Exception e) {
            log.error("Failed to broadcast message via WebSocket for chat {}: {}", 
                chatId, e.getMessage(), e);
        }

        ChatEventDTO event = ChatEventDTO.builder()
            .eventId(eventId)
            .eventType(ChatEventDTO.ChatEventType.MESSAGE_SENT)
            .timestamp(timestamp)
            .chatId(chatId)
            .userId(senderId)
            .username(sender.getUsername())
            .userDisplayName(sender.getName())
            .userAvatarUrl(sender.getAvatarUrl())
            .payload(request.getContent())
            .build();
        
        try {
            chatEventProducer.publishEvent(event);
            log.debug("Published chat event {} to Kafka for chat {}", eventId, chatId);
        } catch (Exception e) {
            log.error("Failed to publish chat event to Kafka for chat {}: {}", 
                chatId, e.getMessage(), e);
        }
    }

    public void broadcastMessage(MessageResponseDTO message) {
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(), message);
    }

    public PagedResponse<MessageResponseDTO> getChatHistory(UUID chatId, Pageable pageable) {
        Page<MessageResponseDTO> page = messageRepo.findByChatIdOrderBySentAtDesc(chatId, pageable)
            .map(messageMapper::toResponse);
        return new PagedResponse<>(page);
    }
}
