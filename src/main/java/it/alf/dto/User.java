package it.alf.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO che rappresenta un utente inviato su Kafka.
 * Viene serializzato in JSON dal producer e deserializzato dal consumer.
 * Fields:
 * - id: identificativo univoco del user
 * - name: nome dell'utente
 * - email: indirizzo email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private String id;
    private String name;
    private String email;
}
