package it.alf.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.alf.dto.GenericEvent;
import it.alf.entity.GenericEventEntity;
import it.alf.repository.GenericEventRepository;

@Service
/**
 * Consumer per eventi generici (`GenericEvent`).
 *
 * I payload vengono serializzati in JSON e memorizzati nella tabella `generic_event`.
 * Questo è utile come esempio per show-how di un event-bus: il consumer non interpreta
 * il payload, lo conserva come stringa JSON per successiva elaborazione.
 */
public class EventListener {
    private final Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final GenericEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public EventListener(GenericEventRepository eventRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.events}", groupId = "events-group", containerFactory = "eventListenerContainerFactory")
    public void listen(GenericEvent event) {
        logger.info("Consumed event: {} type={}", event.getId(), event.getType());
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event.getPayload());
        } catch (JsonProcessingException e) {
            // In caso di problemi di serializzazione, memorizziamo un payload vuoto per non bloccare l'intero flow
            payload = "{}";
        }
        try {
            var entity = new GenericEventEntity(event.getId(), event.getType(), payload);
            eventRepository.save(entity);
            logger.info("Event persisted: {}", event.getId());
        } catch (Exception e) {
            logger.error("Error saving event {}: {}", event.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
