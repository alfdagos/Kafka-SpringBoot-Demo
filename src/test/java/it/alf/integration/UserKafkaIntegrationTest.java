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

import it.alf.dto.User;
import it.alf.entity.UserEntity;
import it.alf.repository.UserRepository;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(partitions = 1, topics = {"users-topic"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserKafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void whenSendUser_thenShouldBePersisted() throws Exception {
        var user = new User("test-u1", "Test User", "test@example.com");

        // wait for users listener to be ready
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            if ("users-group".equals(container.getContainerProperties().getGroupId())) {
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
                break;
            }
        }

        kafkaTemplate.send("users-topic", user.getId(), user).get();

    // wait up to 10 seconds for repository to have the user
    Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        Optional<UserEntity> found = Optional.empty();
        while (Instant.now().isBefore(deadline)) {
            found = userRepository.findById(user.getId());
            if (found.isPresent()) break;
            Thread.sleep(200);
        }

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }
}
