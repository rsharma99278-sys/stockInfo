package com.stockinfo.repository;

import com.stockinfo.entity.Portfolio;
import com.stockinfo.entity.Stock;
import com.stockinfo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUser(User user);
    Optional<Portfolio> findByUserAndStock(User user, Stock stock);
    void deleteByUserAndStock(User user, Stock stock);
}
