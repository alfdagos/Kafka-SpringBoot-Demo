package it.alf.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import it.alf.dto.Order;
import it.alf.entity.OrderEntity;
import it.alf.repository.OrderRepository;

@Service
/**
 * Consumer per messaggi di tipo {@link it.alf.dto.Order}.
 *
 * Elabora l'ordine ricevuto e lo salva come {@link it.alf.entity.OrderEntity} tramite JPA.
 * Le eccezioni non gestite vengono propagate per essere processate dall'error handler centrale
 * (es. invio su DLQ dopo retry).
 */
public class OrderListener {
    private final Logger logger = LoggerFactory.getLogger(OrderListener.class);
    private final OrderRepository orderRepository;

    public OrderListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.orders}", groupId = "orders-group", containerFactory = "orderListenerContainerFactory")
    public void listen(Order order) {
        logger.info("Consumed order: {} for user {} amount={}", order.getId(), order.getUserId(), order.getAmount());
        try {
            var entity = new OrderEntity(order.getId(), order.getUserId(), order.getProduct(), order.getAmount());
            orderRepository.save(entity);
            logger.info("Order persisted: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error saving order {}: {}", order.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
