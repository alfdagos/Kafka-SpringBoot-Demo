package it.alf.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO generico per eventi. Il payload è una mappa generica che può contenere
 * qualsiasi informazione utile all'integrazione (es. userId, metadata, ecc.).
 * Il consumer qui salva il payload come stringa JSON per uso dimostrativo.
 */
public class GenericEvent implements Serializable {
    private String id;
    private String type;
    private Map<String, Object> payload;

    public GenericEvent() {}

    public GenericEvent(String id, String type, Map<String, Object> payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
