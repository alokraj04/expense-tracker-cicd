package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.Exception.BadRequestException;
import com.alok.Expense_Tracker.Exception.ResourceNotFoundException;
import com.alok.Expense_Tracker.Exception.UnauthorizedException;
import com.alok.Expense_Tracker.Repository.*;
import com.alok.Expense_Tracker.Security.RolePermissionMapping;
import com.alok.Expense_Tracker.dto.BudgetRequestDto;
import com.alok.Expense_Tracker.dto.BudgetResponseDto;
import com.alok.Expense_Tracker.dto.DashboardResponseDto;
import com.alok.Expense_Tracker.entity.Budget;
import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import com.alok.Expense_Tracker.entity.type.*;
import com.alok.Expense_Tracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrinciple userPrinciple) {
            return userPrinciple.getUser();
        }

        if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oauthUser) {

            String email = oauthUser.getAttribute("email");

            if (email == null) {
                throw new RuntimeException("Email not found from OAuth provider");
            }

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found in DB"));
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
    }

    @Override
    public BudgetResponseDto createBudget(BudgetRequestDto budgetRequestDto) {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.BUDGET_CREATE)) {
            throw new UnauthorizedException("You are not allowed to create a budget");
        }

        Category category = categoryRepository.findById(budgetRequestDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot use this category");
        }
        if (category.getCategoryType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Budget can only be set for EXPENSE categories");
        }

        Optional<Budget> existingBudget =
                budgetRepository.findByUserAndCategoryAndPeriod(user, category, budgetRequestDto.getPeriod());
        if (existingBudget.isPresent()) {
            throw new BadRequestException("Budget already exists for this category and period");
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setBudgetLimit(budgetRequestDto.getBudgetLimit());
        budget.setPeriod(budgetRequestDto.getPeriod());
        budget.setCreatedAt(LocalDateTime.now());

        budgetRepository.save(budget);
        return getAllBudget().stream()
                .filter(b -> b.getId().equals(budget.getId()))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public BudgetResponseDto updateBudget(Long id, BudgetRequestDto dto) {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.BUDGET_UPDATE)) {
            throw new UnauthorizedException("You are not allowed to update a budget");
        }

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot update this budget");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot use this category");
        }
        if (category.getCategoryType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Budget can only be set for EXPENSE categories");
        }

        Optional<Budget> existing = budgetRepository
                .findByUserAndCategoryAndPeriod(user, category, dto.getPeriod());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new BadRequestException("Budget already exists for this category and period");
        }

        budget.setCategory(category);
        budget.setBudgetLimit(dto.getBudgetLimit());
        budget.setPeriod(dto.getPeriod());
        budget.setUpdatedAt(LocalDateTime.now());

        budgetRepository.save(budget);
        return modelMapper.map(budget, BudgetResponseDto.class);
    }

    @Override
    public void deleteBudget(Long id) {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.BUDGET_DELETE)) {
            throw new UnauthorizedException("You are not allowed to delete a budget");
        }

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot delete this budget");
        }

        budgetRepository.delete(budget);
    }

    @Override
    public List<BudgetResponseDto> getAllBudget() {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.BUDGET_READ)) {
            throw new UnauthorizedException("You are not allowed to view budgets");
        }

        List<Budget> budgets;
        if (user.getRoles().contains(RoleType.ADMIN)) {
            budgets = budgetRepository.findAll();
        } else {
            budgets = budgetRepository.findByUser(user);
        }

        return budgets.stream().map(budget -> {
            Category category = budget.getCategory();
            LocalDateTime start;
            LocalDateTime end;

            if (budget.getPeriod() == BudgetPeriod.MONTHLY) {
                LocalDate today = LocalDate.now();
                start = today.withDayOfMonth(1).atStartOfDay();
                end = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
            } else {
                LocalDate today = LocalDate.now();
                start = today.with(DayOfWeek.MONDAY).atStartOfDay();
                end = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
            }

            BigDecimal spent = expenseRepository
                    .sumByUserAndCategoryAndDateBetween(user, category, start, end);
            if (spent == null) spent = BigDecimal.ZERO;

            BigDecimal limit = budget.getBudgetLimit();
            BigDecimal remaining = limit.subtract(spent);

            BigDecimal percentageUsed = BigDecimal.ZERO;
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = spent.multiply(BigDecimal.valueOf(100))
                        .divide(limit, 2, RoundingMode.HALF_UP);
            }

            BudgetStatus status;
            if (spent.compareTo(limit) >= 0) {
                status = BudgetStatus.EXCEEDED;
            } else if (spent.compareTo(limit.multiply(BigDecimal.valueOf(0.8))) >= 0) {
                status = BudgetStatus.WARNING;
            } else {
                status = BudgetStatus.OK;
            }

            BudgetResponseDto budgetDto = new BudgetResponseDto();
            budgetDto.setId(budget.getId());
            budgetDto.setCategoryName(category.getName());
            budgetDto.setBudgetLimit(limit);
            budgetDto.setSpent(spent);
            budgetDto.setRemaining(remaining);
            budgetDto.setPercentageUsed(percentageUsed);
            budgetDto.setBudgetStatus(status);
            budgetDto.setBudgetPeriod(budget.getPeriod());

            return budgetDto;
        }).toList();
    }

    @Override
    public DashboardResponseDto getDashboard() {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.BUDGET_READ)) {
            throw new UnauthorizedException("You are not allowed to view the dashboard");
        }

        BigDecimal totalIncome = incomeRepository.sumIncomeByUser(user);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpense = expenseRepository.sumExpenseByUser(user);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        List<BudgetResponseDto> budgets = getAllBudget();
        BudgetStatus overallStatus = BudgetStatus.OK;

        for (BudgetResponseDto budget : budgets) {
            if (budget.getBudgetStatus() == BudgetStatus.EXCEEDED) {
                overallStatus = BudgetStatus.EXCEEDED;
                break;
            } else if (budget.getBudgetStatus() == BudgetStatus.WARNING) {
                overallStatus = BudgetStatus.WARNING;
            }
        }

        DashboardResponseDto dto = new DashboardResponseDto();
        dto.setTotalIncome(totalIncome);
        dto.setTotalExpense(totalExpense);
        dto.setNetBalance(netBalance);
        dto.setBudgetStatus(overallStatus);

        return dto;
    }
}



