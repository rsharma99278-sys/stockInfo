package com.stockinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Unified response for a single stock's live price data,
 * regardless of whether it came from the real Finnhub API
 * or from our internal DB-based simulator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStockDto {
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal changePercent;
    private Long volume;

    /** "LIVE" = real Finnhub data, "SIMULATED" = fallback from our DB simulator */
    private String source;
}
