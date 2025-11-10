package it.alf.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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

import it.alf.dto.GenericEvent;
import it.alf.entity.GenericEventEntity;
import it.alf.repository.GenericEventRepository;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(partitions = 1, topics = {"events-topic"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GenericEventKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private GenericEventRepository genericEventRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @AfterEach
    void cleanup() {
        genericEventRepository.deleteAll();
    }

    @Test
    void whenSendGenericEvent_thenShouldBePersisted() throws Exception {
        var payload = Map.<String, Object>of("key", "value");
        var event = new GenericEvent("evt-1", "test-type", payload);

        // wait for events listener to be assigned to partitions
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            if ("events-group".equals(container.getContainerProperties().getGroupId())) {
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
                break;
            }
        }

        kafkaTemplate.send("events-topic", event.getId(), event).get();

    Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        Optional<GenericEventEntity> found = Optional.empty();
        while (Instant.now().isBefore(deadline)) {
            found = genericEventRepository.findById(event.getId());
            if (found.isPresent()) break;
            Thread.sleep(200);
        }

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(event.getType());
        assertThat(found.get().getPayloadJson()).contains("\"key\":\"value\"");
    }
}
