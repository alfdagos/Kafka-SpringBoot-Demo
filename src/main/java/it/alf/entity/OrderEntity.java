package it.alf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private double amount;

    public OrderEntity() {}

    public OrderEntity(String id, String userId, String product, double amount) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.amount = amount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
