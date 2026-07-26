package com.stockinfo.controller;

import com.stockinfo.dto.LiveStockDto;
import com.stockinfo.service.LiveStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kept as a separate, fixed-watchlist endpoint (a quick "market pulse" of a
 * few well-known symbols) alongside the more flexible /api/live/quotes,
 * which accepts any symbol list. Both are backed by the same LiveStockService.
 */
@RestController
@RequiredArgsConstructor
public class LiveStockController {

    private final LiveStockService liveStockService;

    @GetMapping("/api/live-stocks")
    public List<LiveStockDto> getLiveStocks() {
        String symbols = "RELIANCE,TCS,INFY,HDFCBANK,AAPL,MSFT,NVDA,TSLA";
        return liveStockService.getLiveQuotes(symbols);
    }
}
