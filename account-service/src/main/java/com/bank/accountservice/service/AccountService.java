package com.bank.accountservice.service;


import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.dto.AccountRequestDTO;
import com.bank.accountservice.dto.AccountResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponseDTO createAccount(AccountRequestDTO request);
    AccountResponseDTO getAccountById(UUID id);
    List<AccountResponseDTO> getAllAccounts();
    List<AccountResponseDTO> getAccountsByCustomer(String customerReference);
    AccountResponseDTO updateAccountStatus(UUID id, AccountStatus status);
    AccountResponseDTO debit(UUID accountId, double amount, String currency);
    AccountResponseDTO credit(UUID accountId, double amount, String currency);
    void transfer(UUID sourceId, UUID destinationId, double amount, String currency);
}
