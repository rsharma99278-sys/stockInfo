package com.stockinfo.service;

import com.stockinfo.dto.SuggestionDto;
import com.stockinfo.entity.InvestmentSuggestion;
import com.stockinfo.entity.Stock;
import com.stockinfo.entity.SuggestionType;
import com.stockinfo.repository.InvestmentSuggestionRepository;
import com.stockinfo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Very simple rule-based Buy/Hold/Sell suggestion engine.
 * This is intentionally basic - it is meant to demonstrate the concept for
 * an academic project, not to be real financial advice.
 *
 * Rules:
 *  - changePercent <= -3%   -> BUY  (stock dipped, potential value opportunity)
 *  - changePercent >= +5%   -> SELL (stock has rallied hard, consider booking profit)
 *  - otherwise              -> HOLD
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final StockRepository stockRepository;
    private final InvestmentSuggestionRepository suggestionRepository;

    private static final BigDecimal BUY_THRESHOLD = new BigDecimal("-3.0");
    private static final BigDecimal SELL_THRESHOLD = new BigDecimal("5.0");

    public List<SuggestionDto> getAllSuggestions() {
        return stockRepository.findAll().stream()
                .map(this::generateSuggestion)
                .toList();
    }

    public SuggestionDto getSuggestionForStock(String symbol) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new com.stockinfo.exception.ResourceNotFoundException("Stock not found: " + symbol));
        return generateSuggestion(stock);
    }

    /**
     * Recalculates and persists suggestions for every stock. Runs automatically
     * every time stock prices are refreshed by the simulator, and can also be
     * triggered manually via the admin panel.
     */
    @Scheduled(fixedRate = 60000)
    public void refreshAllSuggestions() {
        stockRepository.findAll().forEach(this::generateSuggestion);
    }

    private SuggestionDto generateSuggestion(Stock stock) {
        SuggestionType type;
        String reason;

        BigDecimal change = stock.getChangePercent();

        if (change.compareTo(BUY_THRESHOLD) <= 0) {
            type = SuggestionType.BUY;
            reason = "Price dropped " + change.abs() + "% - potential buying opportunity";
        } else if (change.compareTo(SELL_THRESHOLD) >= 0) {
            type = SuggestionType.SELL;
            reason = "Price surged " + change + "% - consider booking profits";
        } else {
            type = SuggestionType.HOLD;
            reason = "Price movement (" + change + "%) is within normal range";
        }

        InvestmentSuggestion suggestion = suggestionRepository.findByStock(stock)
                .orElse(InvestmentSuggestion.builder().stock(stock).build());
        suggestion.setSuggestion(type);
        suggestion.setReason(reason);
        suggestionRepository.save(suggestion);

        return SuggestionDto.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .suggestion(type.name())
                .reason(reason)
                .build();
    }
}
