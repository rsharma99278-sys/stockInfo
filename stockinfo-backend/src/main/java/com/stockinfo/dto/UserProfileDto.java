package com.stockinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private BigDecimal walletBalance;
    private LocalDateTime memberSince;
}