package it.alf.dto;

import java.io.Serializable;

/**
 * DTO che rappresenta un utente inviato su Kafka.
 * Viene serializzato in JSON dal producer e deserializzato dal consumer.
 * Fields:
 * - id: identificativo univoco del user
 * - name: nome dell'utente
 * - email: indirizzo email
 */
public class User implements Serializable {
    private String id;
    private String name;
    private String email;

    public User() {
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
