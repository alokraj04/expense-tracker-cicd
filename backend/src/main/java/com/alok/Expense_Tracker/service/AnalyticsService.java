package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.dto.CategoryBreakdownResponse;
import com.alok.Expense_Tracker.dto.IncomeExpenseDto;
import com.alok.Expense_Tracker.dto.MonthlySummaryDto;
import com.alok.Expense_Tracker.dto.WeeklySummaryDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface AnalyticsService {
    CategoryBreakdownResponse getCategoryBreakdown();

    List<MonthlySummaryDto> getMonthlySummary();


    List<WeeklySummaryDto> getWeeklySummary();

    List<IncomeExpenseDto> getIncomeVsExpense();

    ByteArrayInputStream generateExcelReport() throws IOException;
}
