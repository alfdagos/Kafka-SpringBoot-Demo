package it.alf.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import it.alf.dto.User;
import it.alf.entity.UserEntity;
import it.alf.repository.UserRepository;

@Service
/**
 * Consumer per messaggi di tipo {@link it.alf.dto.User}.
 *
 * Responsabilità:
 * - deserializzare il payload JSON in {@link it.alf.dto.User}
 * - trasformarlo in {@link it.alf.entity.UserEntity}
 * - salvare l'entità tramite {@link it.alf.repository.UserRepository}
 *
 * Il metodo listener è annotato con {@code @KafkaListener} e viene eseguito nel thread del container.
 */
public class UserListener {
    private final Logger logger = LoggerFactory.getLogger(UserListener.class);
    private final UserRepository userRepository;

    public UserListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.users}", groupId = "users-group", containerFactory = "userListenerContainerFactory")
    public void listen(User user) {
        // Log di ricezione e persistenza. Se la save fallisce l'eccezione viene rilanciata e gestita
        // dal DefaultErrorHandler configurato in KafkaConfig (potenziale invio su DLQ dopo retry).
        logger.info("Consumed user: {} - {}", user.getId(), user.getEmail());
        try {
            var entity = new UserEntity(user.getId(), user.getName(), user.getEmail());
            userRepository.save(entity);
            logger.info("User persisted: {}", user.getId());
        } catch (Exception e) {
            logger.error("Error saving user {}: {}", user.getId(), e.getMessage(), e);
            // Rilancio per permettere all'error handler configurato di gestire il fallimento
            throw e;
        }
    }
}
