package com.stockinfo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockinfo.dto.LiveStockDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/**
 * Talks to the Finnhub real-time quote API.
 * Replaces the old Yahoo Finance integration, which used an
 * unofficial/undocumented endpoint that Yahoo has since locked down
 * (it was returning "Unauthorized" for every request).
 *
 * Finnhub free tier covers US-listed tickers (AAPL, MSFT, TSLA, etc.)
 * well. It does NOT reliably support NSE/BSE (.NS) symbols on the free
 * plan, so Indian stocks are expected to fall back to the DB simulator
 * (handled one layer up, in LiveStockService).
 */
@Component
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${finnhub.api.key:}")
    private String apiKey;

    @Value("${finnhub.api.base-url:https://finnhub.io/api/v1}")
    private String baseUrl;

    /**
     * Fetches a live quote for a single symbol from Finnhub.
     * Returns null if the call fails for any reason (bad symbol,
     * rate limit, network issue, missing API key) so the caller
     * can decide how to fall back.
     */
    public LiveStockDto getQuote(String symbol) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Finnhub API key is not configured; skipping live lookup for {}", symbol);
            return null;
        }

        String url = baseUrl + "/quote?symbol=" + symbol + "&token=" + apiKey;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("Finnhub returned non-success status {} for symbol {}", response.code(), symbol);
                return null;
            }

            JsonNode node = objectMapper.readTree(response.body().string());

            // Finnhub returns all zeros when a symbol is unknown/unsupported
            BigDecimal currentPrice = node.path("c").decimalValue();
            BigDecimal previousClose = node.path("pc").decimalValue();
            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Finnhub had no data for symbol {}", symbol);
                return null;
            }

            BigDecimal changePercent = previousClose.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : currentPrice.subtract(previousClose)
                        .divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

            return LiveStockDto.builder()
                    .symbol(symbol)
                    .companyName(symbol) // Finnhub's quote endpoint doesn't include the name
                    .currentPrice(currentPrice)
                    .previousClose(previousClose)
                    .dayHigh(node.path("h").decimalValue())
                    .dayLow(node.path("l").decimalValue())
                    .changePercent(changePercent.setScale(2, RoundingMode.HALF_UP))
                    .volume(0L) // not provided by the /quote endpoint
                    .source("LIVE")
                    .build();

        } catch (Exception e) {
            log.warn("Finnhub call failed for symbol {}: {}", symbol, e.getMessage());
            return null;
        }
    }
}
