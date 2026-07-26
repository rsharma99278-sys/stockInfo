package com.stockinfo.controller;

import com.stockinfo.dto.ApiResponse;
import com.stockinfo.dto.WatchlistDto;
import com.stockinfo.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<List<WatchlistDto>> getWatchlist(Authentication auth) {
        return ResponseEntity.ok(watchlistService.getUserWatchlist(auth.getName()));
    }

    @PostMapping("/{symbol}")
    public ResponseEntity<WatchlistDto> add(Authentication auth, @PathVariable String symbol) {
        return ResponseEntity.ok(watchlistService.addToWatchlist(auth.getName(), symbol));
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<ApiResponse> remove(Authentication auth, @PathVariable String symbol) {
        watchlistService.removeFromWatchlist(auth.getName(), symbol);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Removed from watchlist").build());
    }
}
