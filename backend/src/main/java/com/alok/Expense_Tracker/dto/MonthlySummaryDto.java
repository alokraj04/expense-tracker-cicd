package com.alok.Expense_Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MonthlySummaryDto {
    private String month;
    private BigDecimal totalExpense;

    public MonthlySummaryDto(String month, BigDecimal totalExpense) {
        this.month = month;
        this.totalExpense = BigDecimal.valueOf(totalExpense.doubleValue());
    }
    public MonthlySummaryDto(String month, Number totalExpense) {
        this.month = month;
        this.totalExpense = BigDecimal.valueOf(totalExpense.doubleValue());
    }


}