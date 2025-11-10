package it.alf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.alf.dto.Order;
import it.alf.producer.OrderProducer;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderProducer producer;

    public OrderController(OrderProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        producer.send(order);
        return ResponseEntity.ok("Order sent to Kafka: " + order.getId());
    }
}
