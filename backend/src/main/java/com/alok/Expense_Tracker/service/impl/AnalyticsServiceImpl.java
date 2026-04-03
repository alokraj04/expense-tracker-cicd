package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.Exception.UnauthorizedException;
import com.alok.Expense_Tracker.Repository.ExpenseRepository;
import com.alok.Expense_Tracker.Repository.IncomeRepository;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.Security.RolePermissionMapping;
import com.alok.Expense_Tracker.dto.*;
import com.alok.Expense_Tracker.entity.Expense;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import com.alok.Expense_Tracker.entity.type.PermissionType;
import com.alok.Expense_Tracker.service.AnalyticsService;
import com.alok.Expense_Tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ExpenseRepository expenseRepository;
    public final IncomeRepository incomeRepository;
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
    public CategoryBreakdownResponse getCategoryBreakdown() {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new UnauthorizedException("You are not allowed to view category breakdown");
        }

        List<CategoryBreakdownDto> breakdown =
                expenseRepository.getCategoryBreakdown(user.getId());

        BigDecimal totalExpense = expenseRepository.getTotalExpense(user.getId());
        totalExpense = (totalExpense != null) ? totalExpense : BigDecimal.ZERO;

        for (CategoryBreakdownDto dto : breakdown) {
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = dto.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalExpense, 2, RoundingMode.HALF_UP);
                dto.setPercentage(percentage);
            } else {
                dto.setPercentage(BigDecimal.ZERO);
            }
        }
        return new CategoryBreakdownResponse(totalExpense, breakdown);
    }

    @Override
    public List<MonthlySummaryDto> getMonthlySummary() {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new UnauthorizedException("You are not allowed to view monthly summary");
        }

        List<MonthlySummaryDto> summary =
                expenseRepository.getMonthlySummary(user.getId());

        for (MonthlySummaryDto dto : summary) {
            if (dto.getTotalExpense() == null) {
                dto.setTotalExpense(BigDecimal.ZERO);
            }
        }
        return summary;
    }

    @Override
    public List<WeeklySummaryDto> getWeeklySummary() {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new UnauthorizedException("You are not allowed to view weekly summary");
        }

        List<Object[]> rawData = expenseRepository.getWeeklySummary(user.getId());

        if (rawData == null || rawData.isEmpty()) {
            return new ArrayList<>();
        }

        List<WeeklySummaryDto> summary = rawData.stream()
                .map(obj -> new WeeklySummaryDto(
                        obj[0] != null ? obj[0].toString() : "N/A",
                        obj[1] != null
                                ? (obj[1] instanceof BigDecimal
                                ? (BigDecimal) obj[1]
                                : new BigDecimal(obj[1].toString()))
                                : BigDecimal.ZERO
                ))
                .toList();

        return summary;
    }

    @Override
    public List<IncomeExpenseDto> getIncomeVsExpense() {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)
                || !RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.INCOME_READ)) {
            throw new UnauthorizedException("You are not allowed to view income vs expense");
        }

        List<Object[]> expenseData = expenseRepository.getMonthlyExpenseRaw(user.getId());
        List<Object[]> incomeData = incomeRepository.getMonthlyIncomeRaw(user.getId());

        Map<String, IncomeExpenseDto> map = new TreeMap<>();

        for (Object[] row : expenseData) {
            String month = (String) row[0];
            BigDecimal expense = (BigDecimal) row[1];
            map.putIfAbsent(month, new IncomeExpenseDto(month, BigDecimal.ZERO, BigDecimal.ZERO));
            map.get(month).setTotalExpense(expense);
        }
        for (Object[] row : incomeData) {
            String month = (String) row[0];
            BigDecimal income = (BigDecimal) row[1];
            map.putIfAbsent(month, new IncomeExpenseDto(month, BigDecimal.ZERO, BigDecimal.ZERO));
            map.get(month).setTotalIncome(income);
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public ByteArrayInputStream generateExcelReport() throws IOException {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new UnauthorizedException("You are not allowed to download reports");
        }

        List<Expense> expenses = expenseRepository.findByUser(user);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expenses");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Category");
            header.createCell(2).setCellValue("Amount");

            int rowIdx = 1;
            for (Expense e : expenses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getExpenseDate().toString());
                row.createCell(1).setCellValue(e.getCategory().getName());
                row.createCell(2).setCellValue(e.getAmount().doubleValue());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
