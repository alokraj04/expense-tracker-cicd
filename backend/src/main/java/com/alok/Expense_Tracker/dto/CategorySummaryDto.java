package com.alok.Expense_Tracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySummaryDto {
    private String categoryName;
    private BigDecimal totalAmount;
    private Long transactionCount;

    public CategorySummaryDto(String categoryName, Number totalAmount, Long transactionCount) {
        this.categoryName = categoryName;
        this.totalAmount = BigDecimal.valueOf(totalAmount.doubleValue());
        this.transactionCount = transactionCount;
    }
}