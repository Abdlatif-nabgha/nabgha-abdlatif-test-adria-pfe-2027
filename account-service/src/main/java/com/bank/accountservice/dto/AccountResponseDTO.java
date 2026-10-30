package com.bank.accountservice.dto;

import com.bank.accountservice.domain.AccountStatus;

import java.util.UUID;

public record AccountResponseDTO (
        UUID id,
        String customerReference,
        AccountStatus status,
        String currency,
        double balance
){}


