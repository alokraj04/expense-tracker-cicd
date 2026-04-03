package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.dto.RecurringExpenseRequestDTO;

public interface RecurringExpenseService {
    void createRecurring(RecurringExpenseRequestDTO recurringExpenseRequestDTO);
}
