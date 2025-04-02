// package org.openlake.workSync.app.kafka.consumer;

// import lombok.RequiredArgsConstructor;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class KafkaConsumerService {

//     private final SimpMessagingTemplate messagingTemplate;

//     @KafkaListener(topics = "${spring.kafka.consumer.group-id}", groupId = "chat-consumer-group")
//     public void consume (String message) {
//         messagingTemplate.convertAndSend("/topic/chat", message);
//     }
// }
