package com.stockinfo.service;

import com.stockinfo.dto.WatchlistDto;
import com.stockinfo.entity.Stock;
import com.stockinfo.entity.User;
import com.stockinfo.entity.Watchlist;
import com.stockinfo.exception.BadRequestException;
import com.stockinfo.exception.ResourceNotFoundException;
import com.stockinfo.repository.UserRepository;
import com.stockinfo.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    public WatchlistDto addToWatchlist(String username, String symbol) {
        User user = getUser(username);
        Stock stock = stockService.findStockEntity(symbol);

        if (watchlistRepository.existsByUserAndStock(user, stock)) {
            throw new BadRequestException(stock.getSymbol() + " is already in your watchlist");
        }

        Watchlist entry = Watchlist.builder().user(user).stock(stock).build();
        watchlistRepository.save(entry);
        return toDto(entry);
    }

    public void removeFromWatchlist(String username, String symbol) {
        User user = getUser(username);
        Stock stock = stockService.findStockEntity(symbol);
        watchlistRepository.deleteByUserAndStock(user, stock);
    }

    public List<WatchlistDto> getUserWatchlist(String username) {
        User user = getUser(username);
        return watchlistRepository.findByUser(user).stream().map(this::toDto).toList();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private WatchlistDto toDto(Watchlist entry) {
        Stock stock = entry.getStock();
        return WatchlistDto.builder()
                .watchlistId(entry.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .changePercent(stock.getChangePercent())
                .build();
    }
}
