package com.bank.transactionservice.client;

import java.util.UUID;

record AccountDto(
        UUID id,
        String customerReference,
        String status, String currency,
        double balance
) {}
