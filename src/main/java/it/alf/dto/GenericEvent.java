package it.alf.dto;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO generico per eventi. Il payload è una mappa generica che può contenere
 * qualsiasi informazione utile all'integrazione (es. userId, metadata, ecc.).
 * Il consumer qui salva il payload come stringa JSON per uso dimostrativo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericEvent implements Serializable {
    private String id;
    private String type;
    private Map<String, Object> payload;
}
