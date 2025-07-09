package org.openlake.workSync.app.kafka;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.openlake.workSync.app.service.ChatService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatKafkaListener {
    private final ChatService chatService;

    @KafkaListener(topicPattern = "chat-.*", groupId = "chat-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MessageResponseDTO message) {
        chatService.broadcastMessage(message);
    }
}
