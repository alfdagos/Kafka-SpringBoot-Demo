package it.alf.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.alf.dto.User;
import it.alf.producer.UserProducer;
import it.alf.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserProducer producer;
    private final UserRepository userRepository;

    public UserController(UserProducer producer, UserRepository userRepository) {
        this.producer = producer;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        producer.send(user);
        return ResponseEntity.ok("User sent to Kafka: " + user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return userRepository.findById(id)
                .map(e -> new User(e.getId(), e.getName(), e.getEmail()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        var list = userRepository.findAll().stream()
                .map(e -> new User(e.getId(), e.getName(), e.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
