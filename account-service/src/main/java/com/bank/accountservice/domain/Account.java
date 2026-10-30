package com.bank.accountservice.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Entity
@ToString
@Builder
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String customerReference;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private String currency;
    private double balance;

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public void debit(double amount, String transactionCurrency) {
        validateTransaction(amount, transactionCurrency);
        if (this.balance < amount) {
            throw new IllegalStateException("Solde insuffisant pour effectuer l'operation.");
        }
        this.balance -= amount;
    }

    public void credit(double amount, String transactionCurrency) {
        validateTransaction(amount, transactionCurrency);
        this.balance += amount;
    }

    private void validateTransaction(double amount, String transactionCurrency) {
        if (!isActive()) {
            throw new IllegalStateException("Le compte n'est pas actif (statut: " + status + ").");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }
        if (transactionCurrency != null && !transactionCurrency.equalsIgnoreCase(this.currency)) {
            throw new IllegalArgumentException("Devise incompatible : attendu " + this.currency + ", reçu " + transactionCurrency);
        }
    }
}