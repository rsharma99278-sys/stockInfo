package com.stockinfo.service;

import com.stockinfo.dto.DashboardResponse;
import com.stockinfo.dto.PortfolioDto;
import com.stockinfo.dto.TransactionDto;
import com.stockinfo.entity.User;
import com.stockinfo.exception.ResourceNotFoundException;
import com.stockinfo.repository.UserRepository;
import com.stockinfo.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    public DashboardResponse getDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<PortfolioDto> holdings = portfolioService.getUserPortfolio(username);
        List<TransactionDto> transactions = transactionService.getUserTransactions(username);

        BigDecimal totalInvested = holdings.stream()
                .map(PortfolioDto::getInvestedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValue = holdings.stream()
                .map(PortfolioDto::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPL = currentValue.subtract(totalInvested);
        BigDecimal totalPLPercent = totalInvested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalPL.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        List<PortfolioDto> topHoldings = holdings.stream()
                .sorted(Comparator.comparing(PortfolioDto::getCurrentValue).reversed())
                .limit(5)
                .toList();

        List<TransactionDto> recentTransactions = transactions.stream().limit(5).toList();

        int watchlistCount = watchlistRepository.findByUser(user).size();

        return DashboardResponse.builder()
                .walletBalance(user.getWalletBalance())
                .totalInvested(totalInvested)
                .currentPortfolioValue(currentValue)
                .totalProfitLoss(totalPL)
                .totalProfitLossPercent(totalPLPercent)
                .totalHoldings(holdings.size())
                .watchlistCount(watchlistCount)
                .topHoldings(topHoldings)
                .recentTransactions(recentTransactions)
                .build();
    }
}
