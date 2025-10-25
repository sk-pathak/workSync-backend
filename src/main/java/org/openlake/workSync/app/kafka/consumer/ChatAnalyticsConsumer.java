package org.openlake.workSync.app.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.dto.ChatEventDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatAnalyticsConsumer {
    
    private final ConcurrentHashMap<UUID, AtomicLong> messageCountByChat = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, AtomicLong> messageCountByUser = new ConcurrentHashMap<>();
    
    @KafkaListener(
        topics = "chat-events",
        groupId = "chat-analytics-consumer-group",
        containerFactory = "chatEventKafkaListenerContainerFactory"
    )
    public void processAnalytics(
            @Payload ChatEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.debug("Analytics consumer processing event {} from partition {} offset {}", 
                event.getEventId(), partition, offset);
            
            switch (event.getEventType()) {
                case MESSAGE_SENT:
                    trackMessageMetrics(event);
                    break;
                case USER_JOINED:
                    trackUserJoined(event);
                    break;
                case USER_LEFT:
                    trackUserLeft(event);
                    break;
                default:
                    log.debug("Event type {} not tracked in analytics", event.getEventType());
            }
            
        } catch (Exception ex) {
            log.error("Failed to process analytics for event {}: {}", 
                event.getEventId(), ex.getMessage(), ex);
        }
    }
    
    private void trackMessageMetrics(ChatEventDTO event) {
        messageCountByChat
            .computeIfAbsent(event.getChatId(), k -> new AtomicLong(0))
            .incrementAndGet();
        
        messageCountByUser
            .computeIfAbsent(event.getUserId(), k -> new AtomicLong(0))
            .incrementAndGet();
        
        long chatTotal = messageCountByChat.get(event.getChatId()).get();
        long userTotal = messageCountByUser.get(event.getUserId()).get();
        
        log.info("Analytics - Chat {}: {} messages | User {}: {} messages", 
            event.getChatId(), chatTotal, event.getUsername(), userTotal);
        
        // TODO:
        // - Analytics dashboard integration
    }
    
    private void trackUserJoined(ChatEventDTO event) {
        log.info("Analytics - User {} joined chat {}", event.getUsername(), event.getChatId());
    }
    
    private void trackUserLeft(ChatEventDTO event) {
        log.info("Analytics - User {} left chat {}", event.getUsername(), event.getChatId());
    }
    
    public ConcurrentHashMap<UUID, AtomicLong> getMessageCountByChat() {
        return new ConcurrentHashMap<>(messageCountByChat);
    }
    
    public ConcurrentHashMap<UUID, AtomicLong> getMessageCountByUser() {
        return new ConcurrentHashMap<>(messageCountByUser);
    }
}
