package it.alf.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per notifiche applicative. Semplice payload con livello e messaggio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {
    private String id;
    private String message;
    private String level;
}
