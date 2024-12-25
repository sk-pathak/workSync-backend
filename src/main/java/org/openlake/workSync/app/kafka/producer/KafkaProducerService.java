package org.openlake.workSync.app.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.entity.ChatEntity;
import org.openlake.workSync.app.repo.ChatMessageRepo;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ChatMessageRepo chatMessageRepo;

    public void sendMessage(Long projectId, String message) {
        ChatEntity chat = new ChatEntity();
        chat.setId(projectId);
        chat.setMessage(message);
        chat.setTimestamp(LocalDateTime.now());
        chatMessageRepo.save(chat);

        String topic = "chat-room-" + projectId;
        kafkaTemplate.send(topic, message);
    }
}
