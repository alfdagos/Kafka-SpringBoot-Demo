package it.alf.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import it.alf.dto.Notification;
import it.alf.entity.NotificationEntity;
import it.alf.repository.NotificationRepository;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(partitions = 1, topics = {"notifications-topic"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @AfterEach
    void cleanup() {
        notificationRepository.deleteAll();
    }

    @Test
    void whenSendNotification_thenShouldBePersisted() throws Exception {
        var notification = new Notification("notif-1", "Test message", "INFO");

        // ensure the listener container is assigned before sending to avoid races
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            if ("notifications-group".equals(container.getContainerProperties().getGroupId())) {
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
                break;
            }
        }

        kafkaTemplate.send("notifications-topic", notification.getId(), notification).get();

    Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        Optional<NotificationEntity> found = Optional.empty();
        while (Instant.now().isBefore(deadline)) {
            found = notificationRepository.findById(notification.getId());
            if (found.isPresent()) break;
            Thread.sleep(200);
        }

        assertThat(found).isPresent();
        assertThat(found.get().getMessage()).isEqualTo(notification.getMessage());
        assertThat(found.get().getLevel()).isEqualTo(notification.getLevel());
    }
}
