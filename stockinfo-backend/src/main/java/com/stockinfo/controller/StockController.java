package com.stockinfo.controller;

import com.stockinfo.dto.StockDto;
import com.stockinfo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockDto>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDto> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockDto>> search(@RequestParam String query) {
        return ResponseEntity.ok(stockService.searchStocks(query));
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<StockDto>> getBySector(@PathVariable String sector) {
        return ResponseEntity.ok(stockService.getStocksBySector(sector));
    }

    @GetMapping("/top-gainers")
    public ResponseEntity<List<StockDto>> getTopGainers() {
        return ResponseEntity.ok(stockService.getTopGainers());
    }

    @GetMapping("/top-losers")
    public ResponseEntity<List<StockDto>> getTopLosers() {
        return ResponseEntity.ok(stockService.getTopLosers());
    }
}
