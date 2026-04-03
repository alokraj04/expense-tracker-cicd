package com.alok.Expense_Tracker.Repository;

import com.alok.Expense_Tracker.entity.Expense;
import com.alok.Expense_Tracker.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

    List<RecurringExpense> findByNextExecutionDateBefore(LocalDateTime date);

    List<RecurringExpense> findByNextExecutionDate(LocalDateTime today);
}
