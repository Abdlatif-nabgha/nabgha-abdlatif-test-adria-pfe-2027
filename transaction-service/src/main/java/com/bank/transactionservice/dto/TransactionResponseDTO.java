package com.bank.transactionservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String merchantCategory,
        String country,
        Instant createdAt
) {}

