package com.bank.transactionservice.web.rest;

import com.bank.transactionservice.dto.TransactionRequestDTO;
import com.bank.transactionservice.dto.TransactionResponseDTO;
import com.bank.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@RequestBody @Valid TransactionRequestDTO request) {
        return new ResponseEntity<>(transactionService.createTransaction(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAll(@RequestParam(required = false) UUID accountId) {
        if (accountId != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId));
        }
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}
