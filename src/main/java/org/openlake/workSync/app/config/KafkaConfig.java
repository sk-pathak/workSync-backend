package org.openlake.workSync.app.config;

import org.openlake.workSync.app.dto.ChatEventDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Bean
    public ProducerFactory<String, ChatEventDTO> chatEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ChatEventDTO> chatEventKafkaTemplate() {
        return new KafkaTemplate<>(chatEventProducerFactory());
    }
    

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatEventDTO> chatEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatEventDTO> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatEventConsumerFactory());
        
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        factory.setConcurrency(3);
        
        return factory;
    }
    
    @Bean
    public DefaultKafkaConsumerFactory<String, ChatEventDTO> chatEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "org.openlake.workSync.app.dto");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");

        return new DefaultKafkaConsumerFactory<>(
            configProps, 
            new StringDeserializer(), 
            new JsonDeserializer<>(ChatEventDTO.class)
        );
    }
    
    @Bean
    public DefaultKafkaConsumerFactory<String, MessageResponseDTO> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "org.openlake.workSync.app.dto");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new JsonDeserializer<>(MessageResponseDTO.class));
    }
}
