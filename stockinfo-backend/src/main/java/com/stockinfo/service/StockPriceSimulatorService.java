package com.stockinfo.service;

import com.stockinfo.entity.Stock;
import com.stockinfo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a live stock market by randomly nudging each stock's price
 * every few seconds. Since this is an academic project with no real
 * market-data subscription, this stands in for a live price feed and
 * keeps the dashboard/charts feeling "alive".
 */
@Service
@RequiredArgsConstructor
public class StockPriceSimulatorService {

    private final StockRepository stockRepository;

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void simulatePriceMovement() {
        List<Stock> stocks = stockRepository.findAll();
        if (stocks.isEmpty()) return;

        for (Stock stock : stocks) {
            // Random fluctuation between -2% and +2%
            double changeFactor = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
            BigDecimal current = stock.getCurrentPrice();
            BigDecimal delta = current.multiply(BigDecimal.valueOf(changeFactor));
            BigDecimal newPrice = current.add(delta).setScale(2, RoundingMode.HALF_UP);

            // Never let price go to zero or below
            if (newPrice.compareTo(BigDecimal.valueOf(1)) < 0) {
                newPrice = BigDecimal.valueOf(1);
            }

            stock.setCurrentPrice(newPrice);
            if (newPrice.compareTo(stock.getDayHigh()) > 0) stock.setDayHigh(newPrice);
            if (newPrice.compareTo(stock.getDayLow()) < 0) stock.setDayLow(newPrice);

            long volumeBump = ThreadLocalRandom.current().nextLong(100, 5000);
            stock.setVolume(stock.getVolume() + volumeBump);
        }

        stockRepository.saveAll(stocks);
    }

    // Resets "previousClose" and day high/low once a day (simulated market open)
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyMarketData() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            stock.setPreviousClose(stock.getCurrentPrice());
            stock.setDayHigh(stock.getCurrentPrice());
            stock.setDayLow(stock.getCurrentPrice());
            stock.setVolume(0L);
        }
        stockRepository.saveAll(stocks);
    }
}
