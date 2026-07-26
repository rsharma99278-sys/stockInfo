package com.stockinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private BigDecimal walletBalance;
    private BigDecimal totalInvested;
    private BigDecimal currentPortfolioValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercent;
    private Integer totalHoldings;
    private Integer watchlistCount;
    private List<PortfolioDto> topHoldings;
    private List<TransactionDto> recentTransactions;
}
