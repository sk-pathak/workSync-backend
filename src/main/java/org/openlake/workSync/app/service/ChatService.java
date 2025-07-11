package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.MessageRequestDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;
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
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final KafkaTemplate<String, MessageResponseDTO> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;

    public void sendMessage(UUID chatId, MessageRequestDTO request, UUID senderId) {
        var sender = userRepo.findById(senderId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(senderId));
        
        var response = MessageResponseDTO.builder()
            .chatId(chatId)
            .senderId(senderId)
            .senderUsername(sender.getUsername())
            .senderName(sender.getName())
            .senderAvatarUrl(sender.getAvatarUrl())
            .content(request.getContent())
            .sentAt(java.time.Instant.now())
            .build();
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, response);
        
        saveMessageAsync(chatId, request, senderId);
    }

    @Async
    public void saveMessageAsync(UUID chatId, MessageRequestDTO request, UUID senderId) {
        try {
            var chat = chatRepo.findById(chatId)
                .orElseThrow(() -> ResourceNotFoundException.chatNotFound(chatId));
            var sender = userRepo.findById(senderId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(senderId));
            var message = messageMapper.toEntity(request);
            message.setChat(chat);
            message.setSender(sender);
            messageRepo.save(message);
        } catch (Exception ex) {
            System.err.println("Failed to save message to database: " + ex.getMessage());
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
