package com.bank.accountservice.service;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.dto.AccountRequestDTO;
import com.bank.accountservice.dto.AccountResponseDTO;
import com.bank.accountservice.exception.AccountNotFoundException;
import com.bank.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;

    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO request) {
        if (request.balance() < 0) {
            throw new IllegalArgumentException("Le solde initial ne peut pas être négatif.");
        }
        if (request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("La devise est obligatoire.");
        }
        if (request.customerReference() == null || request.customerReference().isBlank()) {
            throw new IllegalArgumentException("La référence client est obligatoire.");
        }

        Account account = Account.builder()
                .customerReference(request.customerReference())
                .currency(request.currency().toUpperCase())
                .balance(request.balance())
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = repository.save(account);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(UUID id) {
        Account account = findAccountOrThrow(id);
        return mapToResponseDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAllAccounts() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsByCustomer(String customerReference) {
        return repository.findByCustomerReference(customerReference)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public AccountResponseDTO updateAccountStatus(UUID id, AccountStatus status) {
        Account account = findAccountOrThrow(id);
        account.setStatus(status);
        Account saved = repository.save(account);
        return mapToResponseDTO(saved);
    }

    @Override
    public AccountResponseDTO debit(UUID accountId, double amount, String currency) {
        Account account = findAccountOrThrow(accountId);
        account.debit(amount, currency);
        Account saved = repository.save(account);
        return mapToResponseDTO(saved);
    }

    @Override
    public AccountResponseDTO credit(UUID accountId, double amount, String currency) {
        Account account = findAccountOrThrow(accountId);
        account.credit(amount, currency);
        Account saved = repository.save(account);
        return mapToResponseDTO(saved);
    }

    @Override
    public void transfer(UUID sourceId, UUID destinationId, double amount, String currency) {
        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Impossible d'effectuer un virement vers le même compte.");
        }

        Account sourceAccount = findAccountOrThrow(sourceId);
        Account destinationAccount = findAccountOrThrow(destinationId);

        sourceAccount.debit(amount, currency);
        destinationAccount.credit(amount, currency);

        repository.save(sourceAccount);
        repository.save(destinationAccount);
    }

    private Account findAccountOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Compte introuvable avec l'identifiant : " + id));
    }

    private AccountResponseDTO mapToResponseDTO(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getCustomerReference(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance()
        );
    }
}
