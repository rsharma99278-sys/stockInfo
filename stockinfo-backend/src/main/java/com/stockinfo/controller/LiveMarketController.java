package com.stockinfo.controller;

import com.stockinfo.dto.LiveStockDto;
import com.stockinfo.service.LiveStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveMarketController {

    private final LiveStockService liveStockService;

    @GetMapping("/quotes")
    public List<LiveStockDto> getQuotes(
            @RequestParam(defaultValue =
                    "RELIANCE,TCS,INFY,HDFCBANK,AAPL,MSFT,NVDA")
            String symbols) {

        return liveStockService.getLiveQuotes(symbols);
    }
}
