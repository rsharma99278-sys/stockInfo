package com.stockinfo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateStockRequest {
    @NotBlank
    private String symbol;

    @NotBlank
    private String companyName;

    @NotBlank
    private String sector;

    @NotNull
    @Positive
    private BigDecimal currentPrice;
}
