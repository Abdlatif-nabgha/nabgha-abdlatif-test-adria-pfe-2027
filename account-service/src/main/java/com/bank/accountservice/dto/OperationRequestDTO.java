package com.bank.accountservice.dto;

public record OperationRequestDTO(
        double amount,
        String currency
) {}

