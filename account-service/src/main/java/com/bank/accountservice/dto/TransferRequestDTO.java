package com.bank.accountservice.dto;

import java.util.UUID;

public record TransferRequestDTO(
        UUID sourceAccountId,
        UUID destinationAccountId,
        double amount,
        String currency
) {}

