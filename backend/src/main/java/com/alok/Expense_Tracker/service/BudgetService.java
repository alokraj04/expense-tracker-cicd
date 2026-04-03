package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.dto.BudgetRequestDto;
import com.alok.Expense_Tracker.dto.BudgetResponseDto;
import com.alok.Expense_Tracker.dto.DashboardResponseDto;

import java.util.List;

public interface BudgetService {
    BudgetResponseDto createBudget(BudgetRequestDto budgetRequestDto);

      BudgetResponseDto updateBudget(Long id,BudgetRequestDto budgetRequestDto);

    void deleteBudget(Long id);

    List<BudgetResponseDto> getAllBudget();

    DashboardResponseDto getDashboard();
}
