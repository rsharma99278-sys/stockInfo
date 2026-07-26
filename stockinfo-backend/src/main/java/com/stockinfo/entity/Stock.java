package com.stockinfo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks", uniqueConstraints = @UniqueConstraint(columnNames = "symbol"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String symbol; // e.g. TCS, INFY, RELIANCE

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 50)
    private String sector;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal previousClose;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dayHigh;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dayLow;

    @Column(nullable = false)
    @Builder.Default
    private Long volume = 0L;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal changePercent = BigDecimal.ZERO;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        if (previousClose != null && previousClose.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal change = currentPrice.subtract(previousClose);
            this.changePercent = change
                    .divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
    }
}
