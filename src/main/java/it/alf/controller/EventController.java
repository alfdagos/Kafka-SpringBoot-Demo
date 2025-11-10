package it.alf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.alf.dto.GenericEvent;
import it.alf.producer.EventProducer;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventProducer producer;

    public EventController(EventProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> sendEvent(@RequestBody GenericEvent event) {
        producer.send(event);
        return ResponseEntity.ok("Event sent: " + event.getId());
    }
}
