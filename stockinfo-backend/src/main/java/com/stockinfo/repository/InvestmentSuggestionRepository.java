package com.stockinfo.repository;

import com.stockinfo.entity.InvestmentSuggestion;
import com.stockinfo.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestmentSuggestionRepository extends JpaRepository<InvestmentSuggestion, Long> {
    Optional<InvestmentSuggestion> findByStock(Stock stock);
}
