package org.openlake.workSync.app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.service.ChatService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatKafkaListener {
    private final ChatService chatService;

    @KafkaListener(
        topicPattern = "chat-.*", 
        groupId = "chat-consumer-group", 
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(MessageResponseDTO message) {
        try {
            log.debug("Received message from Kafka for chat: {}", message.getChatId());
            chatService.broadcastMessage(message);
            log.debug("Successfully processed message for chat: {}", message.getChatId());
        } catch (Exception ex) {
            log.error("Failed to broadcast chat message for chat: {}: {}", 
                message.getChatId(), ex.getMessage(), ex);
        }
    }
}
