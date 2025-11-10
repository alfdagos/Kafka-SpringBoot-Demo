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

import it.alf.dto.Order;
import it.alf.entity.OrderEntity;
import it.alf.repository.OrderRepository;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(partitions = 1, topics = {"orders-topic"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
    }

    @Test
    void whenSendOrder_thenShouldBePersisted() throws Exception {
        var order = new Order("ord-1", "test-u1", "Pen", 3.5);

        // wait for listener assignment to avoid race where producer sends before consumer ready
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            if ("orders-group".equals(container.getContainerProperties().getGroupId())) {
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
                break;
            }
        }

        kafkaTemplate.send("orders-topic", order.getId(), order).get();

    Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        Optional<OrderEntity> found = Optional.empty();
        while (Instant.now().isBefore(deadline)) {
            found = orderRepository.findById(order.getId());
            if (found.isPresent()) break;
            Thread.sleep(200);
        }

        assertThat(found).isPresent();
        assertThat(found.get().getProduct()).isEqualTo(order.getProduct());
    }
}
