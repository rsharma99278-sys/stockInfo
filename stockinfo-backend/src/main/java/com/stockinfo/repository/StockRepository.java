package com.stockinfo.repository;

import com.stockinfo.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbolIgnoreCase(String symbol);
    List<Stock> findByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(String companyName, String symbol);
    List<Stock> findBySectorIgnoreCase(String sector);
    boolean existsBySymbolIgnoreCase(String symbol);
}
