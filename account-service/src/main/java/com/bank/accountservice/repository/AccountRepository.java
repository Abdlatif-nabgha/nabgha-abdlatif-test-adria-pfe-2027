package com.bank.accountservice.repository;

import com.bank.accountservice.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findById(UUID id);
    List<Account> findByCustomerReference(String customerReference);
    boolean existsByCustomerReference(String customerReference);
}