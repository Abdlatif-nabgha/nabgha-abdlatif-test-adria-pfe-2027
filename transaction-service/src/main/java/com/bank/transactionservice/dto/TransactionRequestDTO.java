package com.bank.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequestDTO(
        @NotNull(message = "L'identifiant du compte est obligatoire")
        UUID accountId,

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être strictement positif")
        BigDecimal amount,

        @NotNull(message = "La devise est obligatoire")
        String currency,

        String merchantCategory,
        String country
) {}

