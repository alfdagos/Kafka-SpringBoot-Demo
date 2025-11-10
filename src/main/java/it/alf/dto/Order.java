package it.alf.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO che rappresenta un ordine. Usato per dimostrare il flusso asincrono order -> consumer -> persistenza.
 * Fields: id, userId, product, amount
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    private String id;
    private String userId;
    private String product;
    private double amount;
}
