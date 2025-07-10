package org.openlake.workSync.app.kafka;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatKafkaListener {
    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ChatKafkaListener.class);

    @KafkaListener(topicPattern = "chat-.*", groupId = "chat-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MessageResponseDTO message) {
        try {
            chatService.broadcastMessage(message);
        } catch (Exception ex) {
            logger.error("Failed to broadcast chat message", ex);
        }
    }
}
