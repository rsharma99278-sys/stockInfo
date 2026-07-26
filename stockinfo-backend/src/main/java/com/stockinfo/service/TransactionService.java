package com.stockinfo.service;

import com.stockinfo.dto.TransactionDto;
import com.stockinfo.entity.Transaction;
import com.stockinfo.entity.User;
import com.stockinfo.exception.ResourceNotFoundException;
import com.stockinfo.repository.TransactionRepository;
import com.stockinfo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<TransactionDto> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserOrderByTransactionDateDesc(user)
                .stream().map(this::toDto).toList();
    }

    private TransactionDto toDto(Transaction txn) {
        return TransactionDto.builder()
                .id(txn.getId())
                .symbol(txn.getStock().getSymbol())
                .companyName(txn.getStock().getCompanyName())
                .type(txn.getType().name())
                .quantity(txn.getQuantity())
                .pricePerUnit(txn.getPricePerUnit())
                .totalAmount(txn.getTotalAmount())
                .transactionDate(txn.getTransactionDate())
                .build();
    }
}
