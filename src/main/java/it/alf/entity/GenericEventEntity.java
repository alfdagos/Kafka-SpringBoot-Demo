package it.alf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class GenericEventEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String type;

    @Lob
    @Column(nullable = true)
    private String payloadJson;

    public GenericEventEntity() {}

    public GenericEventEntity(String id, String type, String payloadJson) {
        this.id = id;
        this.type = type;
        this.payloadJson = payloadJson;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
