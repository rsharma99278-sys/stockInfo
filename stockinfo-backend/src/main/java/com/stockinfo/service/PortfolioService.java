package com.stockinfo.service;

import com.stockinfo.dto.PortfolioDto;
import com.stockinfo.dto.TransactionRequest;
import com.stockinfo.entity.*;
import com.stockinfo.exception.BadRequestException;
import com.stockinfo.exception.ResourceNotFoundException;
import com.stockinfo.repository.PortfolioRepository;
import com.stockinfo.repository.TransactionRepository;
import com.stockinfo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    @Transactional
    public PortfolioDto buyStock(String username, TransactionRequest request) {
        User user = getUser(username);
        Stock stock = stockService.findStockEntity(request.getSymbol());

        BigDecimal totalCost = stock.getCurrentPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        if (user.getWalletBalance().compareTo(totalCost) < 0) {
            throw new BadRequestException("Insufficient wallet balance to complete this purchase");
        }

        // Deduct funds
        user.setWalletBalance(user.getWalletBalance().subtract(totalCost));
        userRepository.save(user);

        // Update or create portfolio holding, recalculating average buy price
        Portfolio holding = portfolioRepository.findByUserAndStock(user, stock).orElse(null);
        if (holding == null) {
            holding = Portfolio.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(request.getQuantity())
                    .averageBuyPrice(stock.getCurrentPrice())
                    .build();
        } else {
            BigDecimal existingTotal = holding.getAverageBuyPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
            BigDecimal newTotal = existingTotal.add(totalCost);
            int newQuantity = holding.getQuantity() + request.getQuantity();
            holding.setQuantity(newQuantity);
            holding.setAverageBuyPrice(newTotal.divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP));
        }
        portfolioRepository.save(holding);

        // Record transaction
        Transaction txn = Transaction.builder()
                .user(user)
                .stock(stock)
                .type(TransactionType.BUY)
                .quantity(request.getQuantity())
                .pricePerUnit(stock.getCurrentPrice())
                .totalAmount(totalCost)
                .build();
        transactionRepository.save(txn);

        return toDto(holding);
    }

    @Transactional
    public PortfolioDto sellStock(String username, TransactionRequest request) {
        User user = getUser(username);
        Stock stock = stockService.findStockEntity(request.getSymbol());

        Portfolio holding = portfolioRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new BadRequestException("You do not own any shares of " + stock.getSymbol()));

        if (holding.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("You only own " + holding.getQuantity() + " shares of " + stock.getSymbol());
        }

        BigDecimal saleProceeds = stock.getCurrentPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Credit funds
        user.setWalletBalance(user.getWalletBalance().add(saleProceeds));
        userRepository.save(user);

        int remainingQuantity = holding.getQuantity() - request.getQuantity();
        if (remainingQuantity == 0) {
            portfolioRepository.delete(holding);
        } else {
            holding.setQuantity(remainingQuantity);
            portfolioRepository.save(holding);
        }

        // Record transaction
        Transaction txn = Transaction.builder()
                .user(user)
                .stock(stock)
                .type(TransactionType.SELL)
                .quantity(request.getQuantity())
                .pricePerUnit(stock.getCurrentPrice())
                .totalAmount(saleProceeds)
                .build();
        transactionRepository.save(txn);

        if (remainingQuantity == 0) {
            return PortfolioDto.builder()
                    .symbol(stock.getSymbol())
                    .companyName(stock.getCompanyName())
                    .quantity(0)
                    .build();
        }
        return toDto(holding);
    }

    public List<PortfolioDto> getUserPortfolio(String username) {
        User user = getUser(username);
        return portfolioRepository.findByUser(user).stream().map(this::toDto).toList();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private PortfolioDto toDto(Portfolio holding) {
        Stock stock = holding.getStock();
        BigDecimal invested = holding.getAverageBuyPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal currentValue = stock.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal profitLoss = currentValue.subtract(invested);
        BigDecimal profitLossPercent = invested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : profitLoss.divide(invested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return PortfolioDto.builder()
                .portfolioId(holding.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .quantity(holding.getQuantity())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(stock.getCurrentPrice())
                .investedValue(invested)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .profitLossPercent(profitLossPercent)
                .build();
    }
}
