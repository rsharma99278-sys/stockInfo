package com.stockinfo.repository;

import com.stockinfo.entity.Transaction;
import com.stockinfo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
}
