package com.stockinfo.service;

import com.stockinfo.client.MarketDataClient;
import com.stockinfo.dto.LiveStockDto;
import com.stockinfo.entity.Stock;
import com.stockinfo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Single source of truth for "live" stock quotes.
 *
 * Strategy per symbol:
 *  1. Try Finnhub (real data) - works well for US tickers like AAPL, MSFT.
 *  2. If Finnhub has no data (e.g. Indian .NS symbols on the free plan,
 *     rate limit hit, or network failure), fall back to our own DB,
 *     which StockPriceSimulatorService keeps "moving" every 10 seconds.
 *  3. If a symbol exists in neither place, it's simply skipped from
 *     the result rather than breaking the whole response.
 */
@Service
@RequiredArgsConstructor
public class LiveStockService {

    private final MarketDataClient marketDataClient;
    private final StockRepository stockRepository;

    public List<LiveStockDto> getLiveQuotes(String symbolsCsv) {
        List<LiveStockDto> results = new ArrayList<>();

        for (String rawSymbol : symbolsCsv.split(",")) {
            String symbol = rawSymbol.trim();
            if (symbol.isEmpty()) continue;

            LiveStockDto liveResult = marketDataClient.getQuote(symbol);
            if (liveResult != null) {
                results.add(liveResult);
                continue;
            }

            fallbackFromDb(symbol).ifPresent(results::add);
        }

        return results;
    }

    private Optional<LiveStockDto> fallbackFromDb(String symbol) {
        // Indian tickers are usually requested as e.g. "RELIANCE.NS" for the
        // live API; strip the suffix to match how they're stored in our DB.
        String dbSymbol = symbol.replace(".NS", "").replace(".BSE", "");

        return stockRepository.findBySymbolIgnoreCase(dbSymbol)
                .map(this::toDto);
    }

    private LiveStockDto toDto(Stock stock) {
        return LiveStockDto.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .previousClose(stock.getPreviousClose())
                .dayHigh(stock.getDayHigh())
                .dayLow(stock.getDayLow())
                .changePercent(stock.getChangePercent())
                .volume(stock.getVolume())
                .source("SIMULATED")
                .build();
    }
}
