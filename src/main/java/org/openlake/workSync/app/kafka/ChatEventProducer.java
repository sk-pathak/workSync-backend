package org.openlake.workSync.app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.dto.ChatEventDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventProducer {
    
    private static final String CHAT_EVENTS_TOPIC = "chat-events";
    
    private final KafkaTemplate<String, ChatEventDTO> chatEventKafkaTemplate;
    
    public void publishEvent(ChatEventDTO event) {
        String partitionKey = event.getChatId().toString();
        
        CompletableFuture<SendResult<String, ChatEventDTO>> future = 
            chatEventKafkaTemplate.send(CHAT_EVENTS_TOPIC, partitionKey, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Published chat event {} to Kafka: partition={}, offset={}", 
                    event.getEventId(), 
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish chat event {} to Kafka: {}", 
                    event.getEventId(), ex.getMessage(), ex);
            }
        });
    }
    
    public void publishEventSync(ChatEventDTO event) throws Exception {
        String partitionKey = event.getChatId().toString();
        
        SendResult<String, ChatEventDTO> result = 
            chatEventKafkaTemplate.send(CHAT_EVENTS_TOPIC, partitionKey, event).get();
        
        log.info("Synchronously published chat event {} to Kafka: partition={}, offset={}", 
            event.getEventId(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
    }
}
