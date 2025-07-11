package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.MessageRequestDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.mapper.MessageMapper;
import org.openlake.workSync.app.repo.ChatRepo;
import org.openlake.workSync.app.repo.MessageRepo;
import org.openlake.workSync.app.repo.UserRepo;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;

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
        var chat = chatRepo.findById(chatId).orElseThrow(() -> 
            new RuntimeException("Chat not found with ID: " + chatId));
        var sender = userRepo.findById(senderId).orElseThrow(() -> 
            new RuntimeException("User not found with ID: " + senderId));
        var message = messageMapper.toEntity(request);
        message.setChat(chat);
        message.setSender(sender);
        messageRepo.save(message);
        var response = messageMapper.toResponse(message);
        kafkaTemplate.send("chat-" + chatId, response);
    }

    // Called by Kafka listener
    public void broadcastMessage(MessageResponseDTO message) {
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(), message);
    }

    public PagedResponse<MessageResponseDTO> getChatHistory(UUID chatId, Pageable pageable) {
        Page<MessageResponseDTO> page = messageRepo.findByChatIdOrderBySentAtDesc(chatId, pageable).map(messageMapper::toResponse);
        return new PagedResponse<>(page);
    }
}