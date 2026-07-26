package com.stockinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistDto {
    private Long watchlistId;
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
}
