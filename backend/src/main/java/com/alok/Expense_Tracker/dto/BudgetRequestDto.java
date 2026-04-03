package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.type.BudgetPeriod;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequestDto {

    private Long categoryId;
    private BigDecimal budgetLimit;
    private BudgetPeriod period;
}
