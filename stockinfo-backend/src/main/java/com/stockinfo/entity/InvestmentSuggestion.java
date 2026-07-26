package com.stockinfo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores the latest rule-based Buy/Hold/Sell suggestion for a stock.
 */
@Entity
@Table(name = "investment_suggestions", uniqueConstraints = @UniqueConstraint(columnNames = "stock_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false, unique = true)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SuggestionType suggestion;

    @Column(length = 255)
    private String reason;

    private LocalDateTime generatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.generatedAt = LocalDateTime.now();
    }
}
