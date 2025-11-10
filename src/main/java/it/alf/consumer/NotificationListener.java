package it.alf.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import it.alf.dto.Notification;
import it.alf.entity.NotificationEntity;
import it.alf.repository.NotificationRepository;

@Service
/**
 * Consumer per messaggi di tipo {@link it.alf.dto.Notification}.
 *
 * I messaggi di notifica vengono persistiti in H2 per scopi dimostrativi. In un sistema reale
 * il consumer potrebbe inoltrare la notifica a un servizio di push/email.
 */
public class NotificationListener {
    private final Logger logger = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationRepository notificationRepository;

    public NotificationListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.notifications}", groupId = "notifications-group", containerFactory = "notificationListenerContainerFactory")
    public void listen(Notification notification) {
        logger.info("Consumed notification: {} - {}", notification.getId(), notification.getMessage());
        try {
            var entity = new NotificationEntity(notification.getId(), notification.getMessage(), notification.getLevel());
            notificationRepository.save(entity);
            logger.info("Notification persisted: {}", notification.getId());
        } catch (Exception e) {
            logger.error("Error saving notification {}: {}", notification.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
