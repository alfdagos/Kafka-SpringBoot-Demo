package it.alf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.alf.dto.Notification;
import it.alf.producer.NotificationProducer;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationProducer producer;

    public NotificationController(NotificationProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody Notification notification) {
        producer.send(notification);
        return ResponseEntity.ok("Notification sent: " + notification.getId());
    }
}
