package it.alf.dto;

import java.io.Serializable;

/**
 * DTO per notifiche applicative. Semplice payload con livello e messaggio.
 */
public class Notification implements Serializable {
    private String id;
    private String message;
    private String level;

    public Notification() {}

    public Notification(String id, String message, String level) {
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
