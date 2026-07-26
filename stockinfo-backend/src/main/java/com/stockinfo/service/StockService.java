package com.stockinfo.service;

import com.stockinfo.dto.CreateStockRequest;
import com.stockinfo.dto.StockDto;
import com.stockinfo.entity.Stock;
import com.stockinfo.exception.BadRequestException;
import com.stockinfo.exception.ResourceNotFoundException;
import com.stockinfo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream().map(this::toDto).toList();
    }

    public StockDto getStockBySymbol(String symbol) {
        Stock stock = findStockEntity(symbol);
        return toDto(stock);
    }

    public List<StockDto> searchStocks(String query) {
        return stockRepository
                .findByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(query, query)
                .stream().map(this::toDto).toList();
    }

    public List<StockDto> getStocksBySector(String sector) {
        return stockRepository.findBySectorIgnoreCase(sector).stream().map(this::toDto).toList();
    }

    public List<StockDto> getTopGainers() {
        return stockRepository.findAll().stream()
                .sorted((a, b) -> b.getChangePercent().compareTo(a.getChangePercent()))
                .limit(5)
                .map(this::toDto)
                .toList();
    }

    public List<StockDto> getTopLosers() {
        return stockRepository.findAll().stream()
                .sorted((a, b) -> a.getChangePercent().compareTo(b.getChangePercent()))
                .limit(5)
                .map(this::toDto)
                .toList();
    }

    // ---- Admin operations ----

    public StockDto createStock(CreateStockRequest request) {
        if (stockRepository.existsBySymbolIgnoreCase(request.getSymbol())) {
            throw new BadRequestException("Stock with symbol " + request.getSymbol() + " already exists");
        }
        Stock stock = Stock.builder()
                .symbol(request.getSymbol().toUpperCase())
                .companyName(request.getCompanyName())
                .sector(request.getSector())
                .currentPrice(request.getCurrentPrice())
                .previousClose(request.getCurrentPrice())
                .dayHigh(request.getCurrentPrice())
                .dayLow(request.getCurrentPrice())
                .volume(0L)
                .changePercent(BigDecimal.ZERO)
                .build();
        return toDto(stockRepository.save(stock));
    }

    public StockDto updateStockPrice(String symbol, BigDecimal newPrice) {
        Stock stock = findStockEntity(symbol);
        stock.setPreviousClose(stock.getCurrentPrice());
        stock.setCurrentPrice(newPrice);
        if (newPrice.compareTo(stock.getDayHigh()) > 0) stock.setDayHigh(newPrice);
        if (newPrice.compareTo(stock.getDayLow()) < 0) stock.setDayLow(newPrice);
        return toDto(stockRepository.save(stock));
    }

    public void deleteStock(String symbol) {
        Stock stock = findStockEntity(symbol);
        stockRepository.delete(stock);
    }

    // ---- Helpers ----

    public Stock findStockEntity(String symbol) {
        return stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + symbol));
    }

    private StockDto toDto(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .currentPrice(stock.getCurrentPrice())
                .previousClose(stock.getPreviousClose())
                .dayHigh(stock.getDayHigh())
                .dayLow(stock.getDayLow())
                .volume(stock.getVolume())
                .changePercent(stock.getChangePercent())
                .build();
    }
}
