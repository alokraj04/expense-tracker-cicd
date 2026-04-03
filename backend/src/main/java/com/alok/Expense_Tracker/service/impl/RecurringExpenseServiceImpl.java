package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.Repository.CategoryRepository;
import com.alok.Expense_Tracker.Repository.ExpenseRepository;
import com.alok.Expense_Tracker.Repository.RecurringExpenseRepository;
import com.alok.Expense_Tracker.Security.RolePermissionMapping;
import com.alok.Expense_Tracker.dto.RecurringExpenseRequestDTO;
import com.alok.Expense_Tracker.entity.*;
import com.alok.Expense_Tracker.entity.type.CategoryType;
import com.alok.Expense_Tracker.entity.type.PermissionType;
import com.alok.Expense_Tracker.entity.type.RoleType;
import com.alok.Expense_Tracker.service.RecurringExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.alok.Expense_Tracker.entity.type.Frequency.*;

@Service
@RequiredArgsConstructor

public class RecurringExpenseServiceImpl implements RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final CategoryRepository categoryRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrinciple) {
            return ((UserPrinciple) principal).getUser();
        } else if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new RuntimeException("Invalid user principal");
        }
    }

    @Override
    public void createRecurring(RecurringExpenseRequestDTO dto) {

        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_CREATE)) {
            throw new RuntimeException("Not allowed");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (!category.getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(RoleType.ADMIN)) {
            throw new RuntimeException("Unauthorized");
        }

        if (category.getCategoryType() != CategoryType.EXPENSE) {
            throw new RuntimeException("Category must be EXPENSE type");
        }

        RecurringExpense re = new RecurringExpense();

        re.setUser(user);
        re.setAmount(dto.getAmount());
        re.setCategory(category);
        re.setFrequency(dto.getFrequency());

        LocalDateTime startDate = dto.getStartDate();

        switch (dto.getFrequency()) {
            case DAILY -> re.setNextExecutionDate(startDate.plusDays(1));
            case WEEKLY -> re.setNextExecutionDate(startDate.plusWeeks(1));
            case MONTHLY -> re.setNextExecutionDate(startDate.plusMonths(1));
        }

        recurringExpenseRepository.save(re);
    }
}