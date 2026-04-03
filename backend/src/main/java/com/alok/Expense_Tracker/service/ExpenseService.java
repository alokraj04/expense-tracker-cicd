package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.ExpenseRequestDTO;
import com.alok.Expense_Tracker.dto.ExpenseResponseDTO;
import com.alok.Expense_Tracker.entity.RecurringExpense;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseService {


      List<ExpenseResponseDTO> getAllExpenses();

    ExpenseResponseDTO createExpenses(ExpenseRequestDTO expenseRequestDto);

    ExpenseResponseDTO getExpensesById(long id);

    ExpenseResponseDTO updateExpenses(long id, ExpenseRequestDTO expenseRequestDto);

    void deleteExpense(long id);

    List<ExpenseResponseDTO> getExpensesByCategory(Long CategoryId);

    List<ExpenseResponseDTO> getExpensesByDate(LocalDateTime startDate, LocalDateTime endDate);

    List<CategorySummaryDto> getCategorySummary();

    void uploadReciept(Long expenseId, MultipartFile file);

    void createExpenseFromRecurring(RecurringExpense re);
}

