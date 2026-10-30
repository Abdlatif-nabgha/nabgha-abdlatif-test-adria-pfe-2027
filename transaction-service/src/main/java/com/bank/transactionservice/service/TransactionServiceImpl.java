package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountDto;
import com.bank.transactionservice.client.AccountRestClient;
import com.bank.transactionservice.client.OperationRequestDTO;
import com.bank.transactionservice.domain.Transaction;
import com.bank.transactionservice.dto.TransactionRequestDTO;
import com.bank.transactionservice.dto.TransactionResponseDTO;
import com.bank.transactionservice.event.TransactionCreatedEvent;
import com.bank.transactionservice.repository.TransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRestClient accountRestClient;
    private final TransactionEventProducer eventProducer;

    // Plafond configuré (Partie D : bank.transaction.max-amount=1000)
    @Value("${bank.transaction.max-amount:1000}")
    private BigDecimal maxAmount;

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "transactionFallback")
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        // 1. Règle : Montant strictement positif
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }

        // 2. Règle : Respecter le plafond configuré
        if (request.amount().compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Le montant dépasse le plafond autorisé de " + maxAmount);
        }

        // 3. Règle : Consultation du compte & solde via OpenFeign
        AccountDto account = accountRestClient.getAccountById(request.accountId());
        if (account == null) {
            throw new IllegalArgumentException("Le compte n'existe pas.");
        }

        // 4. Règle : Le compte doit être actif
        if (!"ACTIVE".equalsIgnoreCase(account.status())) {
            throw new IllegalStateException("Le compte n'est pas actif (statut: " + account.status() + ").");
        }

        // 5. Règle : La devise de la transaction doit correspondre à celle du compte
        if (!account.currency().equalsIgnoreCase(request.currency())) {
            throw new IllegalArgumentException("Devise incompatible : attendu " + account.currency() + ", reçu " + request.currency());
        }

        // 6. Règle : Vérification du solde suffisant
        if (account.balance() < request.amount().doubleValue()) {
            throw new IllegalStateException("Solde insuffisant.");
        }

        // 7. Enregistrement de la transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.accountId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .merchantCategory(request.merchantCategory())
                .country(request.country())
                .createdAt(Instant.now())
                .build();
        Transaction saved = transactionRepository.save(transaction);

        // 8. Mise à jour du solde du compte (débit)
        accountRestClient.debitAccount(request.accountId(),
                new OperationRequestDTO(request.amount().doubleValue(), request.currency()));

        // 9. Publication Kafka dans bank.transactions.created
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                "TransactionCreated",
                saved.getId(),
                saved.getAccountId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getCreatedAt()
        );
        eventProducer.publishTransactionCreated(event);

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction introuvable avec l'id : " + id));
        return mapToDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByAccount(UUID accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Fallback Resilience4j si account-service est indisponible
    public TransactionResponseDTO transactionFallback(TransactionRequestDTO request, Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Service des comptes indisponible. Veuillez réessayer plus tard. Erreur: " + ex.getMessage());
    }

    private TransactionResponseDTO mapToDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantCategory(),
                transaction.getCountry(),
                transaction.getCreatedAt()
        );
    }
}
