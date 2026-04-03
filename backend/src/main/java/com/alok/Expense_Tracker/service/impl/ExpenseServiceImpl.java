package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.Repository.BudgetRepository;
import com.alok.Expense_Tracker.Repository.CategoryRepository;
import com.alok.Expense_Tracker.Repository.ExpenseRepository;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.Security.RolePermissionMapping;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.ExpenseRequestDTO;
import com.alok.Expense_Tracker.dto.ExpenseResponseDTO;
import com.alok.Expense_Tracker.entity.*;
import com.alok.Expense_Tracker.entity.type.PermissionType;
import com.alok.Expense_Tracker.entity.type.RoleType;
import com.alok.Expense_Tracker.service.EmailService;
import com.alok.Expense_Tracker.service.ExpenseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final EmailService emailService;
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
    @PostConstruct
    public void setupModelMapper() {

        modelMapper.getConfiguration()
                .setAmbiguityIgnored(true);

        modelMapper.typeMap(Expense.class, ExpenseResponseDTO.class)
                .addMappings(mapper -> mapper.map(
                        src -> src.getCategory().getName(),
                        ExpenseResponseDTO::setCategoryName
                ));
    }

    @Override
    public ExpenseResponseDTO createExpenses(ExpenseRequestDTO expenseRequestDto) {
        User user = getCurrentUser();
//        if (!RolePermissionMapping.hasPermission(user.getRole(), PermissionType.EXPENSE_CREATE)) {
//            throw new RuntimeException("Not allowed");
//        }
        Category category = categoryRepository.findById(expenseRequestDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("category not found"));

        Expense expense = Expense.builder()
                .amount(expenseRequestDto.getAmount())
                .user(user)
                .note(expenseRequestDto.getNote())
                .expenseDate(expenseRequestDto.getExpenseDate())
                .category(category)
                .build();

        Expense updatedExpense = expenseRepository.save(expense);
        return modelMapper.map(updatedExpense, ExpenseResponseDTO.class);
    }

    @Override
    public ExpenseResponseDTO getExpensesById(long id) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new RuntimeException("Not allowed");
        }
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("expense not found"));
        if (!expense.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new RuntimeException("Unauthorized access");
        }

        return modelMapper.map(expense, ExpenseResponseDTO.class);
    }

    @Override
    public ExpenseResponseDTO updateExpenses(long id, ExpenseRequestDTO expenseRequestDto) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_UPDATE)) {
            throw new RuntimeException("Not allowed");
        }
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("expense not found"));
        if (!expense.getUser().getId().equals(user.getId())&& !user.getRoles().contains(RoleType.ADMIN)) {
            throw new RuntimeException("Unauthorized access");
        }
        if (expenseRequestDto.getAmount() != null) {
            expense.setAmount(expenseRequestDto.getAmount());
        }
        if (expenseRequestDto.getExpenseDate() != null) {
            expense.setExpenseDate(expenseRequestDto.getExpenseDate());
        }
        if (expenseRequestDto.getNote() != null) {
            expense.setNote(expenseRequestDto.getNote());
        }
        if (expenseRequestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(expenseRequestDto.getCategoryId()).orElseThrow(() -> new RuntimeException("category not found"));
            expense.setCategory(category);
        }


        Expense updatedExpense = expenseRepository.save(expense);

        return modelMapper.map(updatedExpense, ExpenseResponseDTO.class);
    }

    @Override
    public void deleteExpense(long id) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_DELETE)) {
            throw new RuntimeException("Not allowed");
        }
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("expense not found"));
        if (!expense.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new RuntimeException("Unauthorized access");
        }
        expenseRepository.delete(expense);
    }

    @Override
    public void uploadReciept(Long expenseId, MultipartFile file) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_UPDATE)) {
            throw new RuntimeException("Not allowed");
        }

        if (!expense.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new RuntimeException("Unauthorized");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.startsWith("image/") &&
                        !contentType.equals("application/pdf"))) {
            throw new RuntimeException("Only image or PDF allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File too large");
        }

        try {
            String uploadDir = "uploads/";
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path path = Paths.get(uploadDir, fileName);

            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            expense.setReceiptUrl(path.toString());
            expenseRepository.save(expense);

        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }

    }

    public List<CategorySummaryDto> getCategorySummary() {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new RuntimeException("Not allowed");
        }
        if (user.getRoles().contains(RoleType.ADMIN)) {
            return expenseRepository.getCategorySummaryForAll();
        }
        return expenseRepository.getCategorySummary(user);
    }

    public List<ExpenseResponseDTO> getAllExpenses() {

        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new RuntimeException("Not allowed");
        }
       List<Expense> expenses;
        if (user.getRoles().contains(RoleType.ADMIN)) {
            expenses = expenseRepository.findAll();
        } else {
            expenses = expenseRepository.findByUser(user);
        }

        return expenses.stream().map(expense -> modelMapper.map(expense, ExpenseResponseDTO.class)).toList();
    }

    public List<ExpenseResponseDTO> getExpensesByDate(LocalDateTime startDate, LocalDateTime endDate) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new RuntimeException("Not allowed");
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new RuntimeException("Invalid Date Entered");
        }
        List<Expense> expenses;

        if (user.getRoles().contains(RoleType.ADMIN)) {
            expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        } else {
            expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);
        }
        return expenses.stream().map(expense -> modelMapper.map(expense, ExpenseResponseDTO.class)).toList();
    }

    public List<ExpenseResponseDTO> getExpensesByCategory(Long id) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.EXPENSE_READ)) {
            throw new RuntimeException("Not allowed");
        }
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("category not found"));
        List<Expense> expenses;

        if (user.getRoles().contains(RoleType.ADMIN)) {
            expenses = expenseRepository.findByCategory_Id(id);
        } else {
            expenses = expenseRepository.findByUserAndCategory_Id(user, id);
        }

        return expenses.stream().map(expense -> modelMapper.map(expense, ExpenseResponseDTO.class)).toList();
    }

    public void createExpenseFromRecurring(RecurringExpense re) {

        Expense expense = new Expense();
        expense.setAmount(re.getAmount());
        expense.setCategory(re.getCategory());
        expense.setUser(re.getUser());
        expense.setExpenseDate(LocalDateTime.now());

        expenseRepository.save(expense);
    }

}


















