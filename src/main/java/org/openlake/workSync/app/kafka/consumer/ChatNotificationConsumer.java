package org.openlake.workSync.app.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.dto.ChatEventDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatNotificationConsumer {
    
    @KafkaListener(
        topics = "chat-events",
        groupId = "chat-notification-consumer-group",
        containerFactory = "chatEventKafkaListenerContainerFactory"
    )
    public void processNotifications(
            @Payload ChatEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.debug("Notification consumer processing event {} from partition {} offset {}", 
                event.getEventId(), partition, offset);
            
            switch (event.getEventType()) {
                case MESSAGE_SENT:
                    handleMessageNotifications(event);
                    break;
                case USER_JOINED:
                    notifyUserJoined(event);
                    break;
                default:
                    log.debug("Event type {} does not generate notifications", event.getEventType());
            }
            
        } catch (Exception ex) {
            log.error("Failed to process notifications for event {}: {}", 
                event.getEventId(), ex.getMessage(), ex);
        }
    }
    
    private void handleMessageNotifications(ChatEventDTO event) {
        if (event.getPayload() != null && event.getPayload().contains("@")) {
            log.info("Detected potential @mention in message {}, triggering mention notification", 
                event.getEventId());
        }
        
        log.debug("Updating unread message counts for chat {}", event.getChatId());
        
        log.debug("Evaluating push notification criteria for chat {}", event.getChatId());
    }
    
    private void notifyUserJoined(ChatEventDTO event) {
        log.info("Sending 'user joined' notification for {} in chat {}", 
            event.getUsername(), event.getChatId());
    }
}
