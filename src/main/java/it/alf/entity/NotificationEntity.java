package it.alf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String level;

    public NotificationEntity() {}

    public NotificationEntity(String id, String message, String level) {
        this.id = id;
        this.message = message;
        this.level = level;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
