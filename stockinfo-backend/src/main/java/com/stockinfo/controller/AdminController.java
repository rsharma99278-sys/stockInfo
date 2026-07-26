package com.stockinfo.controller;

import com.stockinfo.dto.CreateStockRequest;
import com.stockinfo.dto.StockDto;
import com.stockinfo.repository.TransactionRepository;
import com.stockinfo.repository.UserRepository;
import com.stockinfo.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-only endpoints. Access restricted to users with ROLE_ADMIN
 * (enforced in SecurityConfig on the /api/admin/** path).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StockService stockService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping("/stocks")
    public ResponseEntity<StockDto> createStock(@Valid @RequestBody CreateStockRequest request) {
        return ResponseEntity.ok(stockService.createStock(request));
    }

    @PutMapping("/stocks/{symbol}/price")
    public ResponseEntity<StockDto> updatePrice(@PathVariable String symbol, @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(stockService.updateStockPrice(symbol, body.get("price")));
    }

    @DeleteMapping("/stocks/{symbol}")
    public ResponseEntity<Void> deleteStock(@PathVariable String symbol) {
        stockService.deleteStock(symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalStocks", stockService.getAllStocks().size());
        stats.put("totalTransactions", transactionRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("fullName", u.getFullName());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("walletBalance", u.getWalletBalance());
            m.put("enabled", u.isEnabled());
            return m;
        }).toList();
        return ResponseEntity.ok(users);
    }
}
