package com.bank.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "account-service", url = "${account-service.url:http://localhost:8081}")
public interface AccountRestClient {

    @GetMapping("/api/v1/accounts/{id}")
    AccountDto getAccountById(@PathVariable("id") UUID id);

    @PostMapping("/api/v1/accounts/{id}/debit")
    AccountDto debitAccount(@PathVariable("id") UUID id, @RequestBody OperationRequestDTO request);
}