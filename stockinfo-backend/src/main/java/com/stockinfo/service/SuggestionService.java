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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final StockRepository stockRepository;
    private final InvestmentSuggestionRepository suggestionRepository;

    private static final BigDecimal BUY_THRESHOLD = new BigDecimal("-3.0");
    private static final BigDecimal SELL_THRESHOLD = new BigDecimal("5.0");

    public List<SuggestionDto> getAllSuggestions() {
        return stockRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public SuggestionDto getSuggestionForStock(String symbol) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new com.stockinfo.exception.ResourceNotFoundException("Stock not found: " + symbol));
        return toDto(stock);
    }

    @Scheduled(fixedRate = 60000)
    public void refreshAllSuggestions() {
        List<Stock> stocks = stockRepository.findAll();

        Map<Long, InvestmentSuggestion> existingByStockId = suggestionRepository.findAll().stream()
                .collect(Collectors.toMap(s -> s.getStock().getId(), Function.identity(), (a, b) -> a));

        List<InvestmentSuggestion> toSave = stocks.stream().map(stock -> {
            SuggestionResult result = computeSuggestion(stock);
            InvestmentSuggestion suggestion = existingByStockId.getOrDefault(
                    stock.getId(), InvestmentSuggestion.builder().stock(stock).build());
            suggestion.setSuggestion(result.type());
            suggestion.setReason(result.reason());
            return suggestion;
        }).toList();

        suggestionRepository.saveAll(toSave);
    }

    private SuggestionDto toDto(Stock stock) {
        SuggestionResult result = computeSuggestion(stock);
        return SuggestionDto.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .suggestion(result.type().name())
                .reason(result.reason())
                .build();
    }

    private SuggestionResult computeSuggestion(Stock stock) {
        BigDecimal change = stock.getChangePercent();

        if (change.compareTo(BUY_THRESHOLD) <= 0) {
            return new SuggestionResult(SuggestionType.BUY,
                    "Price dropped " + change.abs() + "% - potential buying opportunity");
        } else if (change.compareTo(SELL_THRESHOLD) >= 0) {
            return new SuggestionResult(SuggestionType.SELL,
                    "Price surged " + change + "% - consider booking profits");
        } else {
            return new SuggestionResult(SuggestionType.HOLD,
                    "Price movement (" + change + "%) is within normal range");
        }
    }

    private record SuggestionResult(SuggestionType type, String reason) {}
}