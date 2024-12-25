package org.openlake.workSync.app.controller.rest;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.kafka.producer.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/{projectId}")
    public ResponseEntity<Void> sendMessage(@PathVariable Long projectId, @RequestBody String message) {
        kafkaProducerService.sendMessage(projectId, message);
        return ResponseEntity.ok().build();
    }
}
