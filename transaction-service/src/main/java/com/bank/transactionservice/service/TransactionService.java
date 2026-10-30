package com.bank.transactionservice.service;

import com.bank.transactionservice.dto.TransactionRequestDTO;
import com.bank.transactionservice.dto.TransactionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    /**
     * Enregistre une nouvelle transaction après vérification des règles métier,
     * met à jour le solde du compte et publie un événement Kafka.
     */
    TransactionResponseDTO createTransaction(TransactionRequestDTO request);

    /**
     * Récupère une transaction par son identifiant.
     */
    TransactionResponseDTO getTransactionById(UUID id);

    /**
     * Récupère toutes les transactions enregistrées.
     */
    List<TransactionResponseDTO> getAllTransactions();

    /**
     * Récupère toutes les transactions associées à un compte donné.
     */
    List<TransactionResponseDTO> getTransactionsByAccount(UUID accountId);
}
