package org.openlake.workSync.app.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.domain.entity.Chat;
import org.openlake.workSync.app.domain.entity.Message;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.dto.ChatEventDTO;
import org.openlake.workSync.app.repo.ChatRepo;
import org.openlake.workSync.app.repo.MessageRepo;
import org.openlake.workSync.app.repo.UserRepo;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatPersistenceConsumer {
    
    private final MessageRepo messageRepo;
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    
    @KafkaListener(
        topics = "chat-events",
        groupId = "chat-persistence-consumer-group",
        containerFactory = "chatEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void persistChatEvent(
            @Payload ChatEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Persistence consumer processing event {} from partition {} offset {}", 
                event.getEventId(), partition, offset);
            
            switch (event.getEventType()) {
                case MESSAGE_SENT:
                    persistMessage(event);
                    break;
                case MESSAGE_DELETED:
                    deleteMessage(event);
                    break;
                case MESSAGE_EDITED:
                    editMessage(event);
                    break;
                default:
                    log.debug("Event type {} does not require persistence", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            
            log.debug("Successfully persisted chat event {}", event.getEventId());
            
        } catch (Exception ex) {
            log.error("Failed to persist chat event {} from partition {} offset {}: {}", 
                event.getEventId(), partition, offset, ex.getMessage(), ex);
            throw ex;
        }
    }
    
    private void persistMessage(ChatEventDTO event) {
        if (messageRepo.existsByEventId(event.getEventId())) {
            log.debug("Message with eventId {} already exists, skipping", event.getEventId());
            return;
        }
        
        Chat chat = chatRepo.findById(event.getChatId())
            .orElseThrow(() -> new RuntimeException("Chat not found: " + event.getChatId()));
        
        User sender = userRepo.findById(event.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found: " + event.getUserId()));
        
        Message message = new Message();
        message.setEventId(event.getEventId());
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(event.getPayload());
        message.setSentAt(event.getTimestamp());
        
        messageRepo.save(message);
        
        log.info("Persisted message {} to database for chat {}", event.getEventId(), event.getChatId());
    }
    
    private void deleteMessage(ChatEventDTO event) {
        log.info("Message deletion not yet implemented for event {}", event.getEventId());
    }
    
    private void editMessage(ChatEventDTO event) {
        log.info("Message editing not yet implemented for event {}", event.getEventId());
    }
}
