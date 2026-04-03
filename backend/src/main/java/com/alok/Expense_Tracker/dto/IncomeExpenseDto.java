package com.alok.Expense_Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeExpenseDto {

    private String month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;


}
