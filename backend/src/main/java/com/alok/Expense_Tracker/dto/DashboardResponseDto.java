package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDto {


        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netBalance    ;
        private BudgetStatus budgetStatus;


    }


