package com.bank.accountservice.service;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.dto.AccountRequestDTO;
import com.bank.accountservice.dto.AccountResponseDTO;
import com.bank.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;

    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO request) {
        if (repository.existsByCustomerReference(request.customerReference())) {
            throw new IllegalArgumentException("You already have an account");
        }

        if (request.balance() < 0) {
            throw new IllegalArgumentException("Initial Sold can't be negative");
        }
        if (request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (request.customerReference() == null || request.customerReference().isBlank()) {
            throw new IllegalArgumentException("Customer reference is required");
        }

        Account account = Account.builder()
                .customerReference(request.customerReference())
                .currency(request.currency())
                .balance(request.balance())
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = repository.save(account);

        return mapToResponseDTO(savedAccount);
    }

    // Méthode utilitaire de mapping (très pratique pour la réutiliser dans toutes vos méthodes)
    private AccountResponseDTO mapToResponseDTO(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getCustomerReference(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance()
        );
    }

    @Override
    public AccountResponseDTO getAccountById(UUID id) {
        return null;
    }

    @Override
    public AccountResponseDTO getAllAccounts() {
        return null;
    }

    @Override
    public List<AccountResponseDTO> getAccountsByCustomer(String customerReference) {
        return List.of();
    }

    @Override
    public AccountResponseDTO updateAccountStatus(UUID id, AccountStatus status) {
        return null;
    }

    @Override
    public AccountResponseDTO debit(UUID accountId, double amount, String currency) {
        return null;
    }

    @Override
    public AccountResponseDTO credit(UUID accountId, double amount, String currency) {
        return null;
    }

    @Override
    public void transfer(UUID sourceId, UUID destinationId, double amount, String currency) {

    }
}
