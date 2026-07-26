package com.stockinfo.config;

import com.stockinfo.entity.Role;
import com.stockinfo.entity.Stock;
import com.stockinfo.entity.User;
import com.stockinfo.repository.StockRepository;
import com.stockinfo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with a default admin account and a starter list of
 * stocks the first time the app runs, so the project is demo-ready
 * immediately after cloning without any manual SQL.
 *
 * Stock data is loaded from stock-seed-data.csv (real, well-known Indian
 * NSE-listed and global companies) rather than being hardcoded here, so
 * the list can grow without touching Java code.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedStocks();
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) return;

        User admin = User.builder()
                .fullName("System Administrator")
                .username("admin")
                .email("admin@stockinfo.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .walletBalance(new BigDecimal("1000000.00"))
                .build();

        userRepository.save(admin);
        System.out.println(">>> Seeded default admin -> username: admin | password: admin123");
    }

    private void seedStocks() {
        if (stockRepository.count() > 0) return;

        List<Stock> stocks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("stock-seed-data.csv").getInputStream(),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // skip header row
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                // columns: symbol,companyName,sector,exchange,startingPrice
                String[] cols = line.split(",");
                if (cols.length < 5) continue;

                String symbol = cols[0].trim();
                String companyName = cols[1].trim();
                String sector = cols[2].trim();
                // cols[3] = exchange (NSE/NASDAQ/NYSE) - not stored on Stock yet
                BigDecimal price = new BigDecimal(cols[4].trim());

                stocks.add(Stock.builder()
                        .symbol(symbol)
                        .companyName(companyName)
                        .sector(sector)
                        .currentPrice(price)
                        .previousClose(price)
                        .dayHigh(price)
                        .dayLow(price)
                        .volume(0L)
                        .changePercent(BigDecimal.ZERO)
                        .build());
            }
        } catch (Exception e) {
            System.err.println(">>> Failed to load stock-seed-data.csv: " + e.getMessage());
            return;
        }

        stockRepository.saveAll(stocks);
        System.out.println(">>> Seeded " + stocks.size() + " real companies (Indian NSE + global mix)");
    }
}
