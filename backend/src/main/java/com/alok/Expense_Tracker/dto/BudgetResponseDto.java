package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.BudgetPeriod;
import com.alok.Expense_Tracker.entity.type.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponseDto {

    private Long id;
    private String categoryName;
    private BigDecimal budgetLimit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BudgetStatus budgetStatus;
    private BudgetPeriod budgetPeriod;
    private BigDecimal percentageUsed;
}
