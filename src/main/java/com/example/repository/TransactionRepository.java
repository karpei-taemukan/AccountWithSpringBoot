package com.example.repository;

import com.example.domain.Account;
import com.example.domain.AccountUser;
import com.example.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {
// mapper 처럼 구현체는 직업 안만듦
    Optional<Transaction> findByTransactionId(String transactionId);
    // transactionId 컬럼을 통해 SELECT
}