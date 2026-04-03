package com.alok.Expense_Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBreakdownDto {
    private String category;
    private BigDecimal totalAmount;
    private BigDecimal percentage;

    public CategoryBreakdownDto(String category, BigDecimal totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }
}