package it.alf.dto;

import java.io.Serializable;

/**
 * DTO che rappresenta un ordine. Usato per dimostrare il flusso asincrono order -> consumer -> persistenza.
 * Fields: id, userId, product, amount
 */
public class Order implements Serializable {
    private String id;
    private String userId;
    private String product;
    private double amount;

    public Order() {}

    public Order(String id, String userId, String product, double amount) {
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
