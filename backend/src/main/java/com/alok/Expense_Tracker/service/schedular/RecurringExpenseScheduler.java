package com.alok.Expense_Tracker.service.schedular;

import com.alok.Expense_Tracker.Repository.RecurringExpenseRepository;
import com.alok.Expense_Tracker.entity.RecurringExpense;
import com.alok.Expense_Tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


    @Service
    @RequiredArgsConstructor
    public class RecurringExpenseScheduler {

        private final RecurringExpenseRepository recurringExpenseRepository;
        private final ExpenseService expenseService;

        @Scheduled(cron = "0 0 0 * * ?") // runs every day at midnight
        public void processRecurringExpenses() {

            LocalDateTime now = LocalDateTime.now();

            List<RecurringExpense> list =
                    recurringExpenseRepository.findByNextExecutionDateBefore(now);

            for (RecurringExpense re : list) {

                expenseService.createExpenseFromRecurring(re);
                switch (re.getFrequency()) {
                    case DAILY -> re.setNextExecutionDate(now.plusDays(1));
                    case WEEKLY -> re.setNextExecutionDate(now.plusWeeks(1));
                    case MONTHLY -> re.setNextExecutionDate(now.plusMonths(1));
                }

                recurringExpenseRepository.save(re);
            }
        }
    }

