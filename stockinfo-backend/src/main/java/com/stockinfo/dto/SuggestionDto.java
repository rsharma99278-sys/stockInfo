package com.stockinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionDto {
    private String symbol;
    private String companyName;
    private String suggestion; // BUY / HOLD / SELL
    private String reason;
}
