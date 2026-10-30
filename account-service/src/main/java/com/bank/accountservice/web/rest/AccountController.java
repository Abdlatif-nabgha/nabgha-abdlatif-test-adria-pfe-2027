package com.bank.accountservice.web.rest;

import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.dto.AccountRequestDTO;
import com.bank.accountservice.dto.AccountResponseDTO;
import com.bank.accountservice.dto.OperationRequestDTO;
import com.bank.accountservice.dto.TransferRequestDTO;
import com.bank.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountRequestDTO request) {
        AccountResponseDTO created = accountService.createAccount(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts(
            @RequestParam(required = false) String customerReference) {
        if (customerReference != null && !customerReference.isBlank()) {
            return ResponseEntity.ok(accountService.getAccountsByCustomer(customerReference));
        }
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/customer/{customerReference}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByCustomer(
            @PathVariable String customerReference) {
        return ResponseEntity.ok(accountService.getAccountsByCustomer(customerReference));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(
            @PathVariable UUID id,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(id, status));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<AccountResponseDTO> debit(
            @PathVariable UUID id,
            @RequestBody OperationRequestDTO request) {
        return ResponseEntity.ok(accountService.debit(id, request.amount(), request.currency()));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<AccountResponseDTO> credit(
            @PathVariable UUID id,
            @RequestBody OperationRequestDTO request) {
        return ResponseEntity.ok(accountService.credit(id, request.amount(), request.currency()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequestDTO request) {
        accountService.transfer(
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency()
        );
        return ResponseEntity.noContent().build();
    }
}
