package com.stockinfo.controller;

import com.stockinfo.dto.PortfolioDto;
import com.stockinfo.dto.TransactionRequest;
import com.stockinfo.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<PortfolioDto>> getPortfolio(Authentication auth) {
        return ResponseEntity.ok(portfolioService.getUserPortfolio(auth.getName()));
    }

    @PostMapping("/buy")
    public ResponseEntity<PortfolioDto> buy(Authentication auth, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(portfolioService.buyStock(auth.getName(), request));
    }

    @PostMapping("/sell")
    public ResponseEntity<PortfolioDto> sell(Authentication auth, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(portfolioService.sellStock(auth.getName(), request));
    }
}
