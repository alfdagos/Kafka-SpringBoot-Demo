package it.alf.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import it.alf.dto.User;

@Service
public class UserProducer {
    private final Logger logger = LoggerFactory.getLogger(UserProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.users}")
    private String topic;

    public UserProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(User user) {
        logger.info("Sending user to topic {}: {}", topic, user.getId());
        kafkaTemplate.send(topic, user.getId(), user);
    }
}
