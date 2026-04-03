package com.alok.Expense_Tracker.controller;

import com.alok.Expense_Tracker.Repository.ExpenseRepository;
import com.alok.Expense_Tracker.Repository.RecurringExpenseRepository;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.ExpenseRequestDTO;
import com.alok.Expense_Tracker.dto.ExpenseResponseDTO;
import com.alok.Expense_Tracker.dto.RecurringExpenseRequestDTO;
import com.alok.Expense_Tracker.entity.Expense;
import com.alok.Expense_Tracker.entity.RecurringExpense;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import com.alok.Expense_Tracker.service.ExpenseService;
import com.alok.Expense_Tracker.service.RecurringExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final RecurringExpenseService recurringExpenseService;
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

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpensesAdmin() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @GetMapping("/audit/all")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpensesForAudit() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> showAllExpenses() {
        List<ExpenseResponseDTO> response= expenseService.getAllExpenses();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> addExpense(@RequestBody ExpenseRequestDTO expenseRequestDto){
        return ResponseEntity.ok(expenseService.createExpenses(expenseRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(@PathVariable long id){
        return ResponseEntity.ok(expenseService.getExpensesById(id));
    }
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpenseById(@PathVariable long id, @RequestBody ExpenseRequestDTO expenseRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.updateExpenses(id,expenseRequestDto));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public void deleteExpenseById(@PathVariable long id){
        expenseService.deleteExpense(id);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByFromDate(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate){
        return ResponseEntity.ok(expenseService.getExpensesByDate(startDate,endDate));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByCategory(@PathVariable Long categoryId){
        return ResponseEntity.ok(expenseService.getExpensesByCategory(categoryId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CategorySummaryDto>> getSummary(){
        return ResponseEntity.ok(expenseService.getCategorySummary());
    }

    @PostMapping("/{expenseId}/receipt")
    public ResponseEntity<String> uploadReciept(@PathVariable Long expenseId, @RequestParam("file") MultipartFile file){
         expenseService.uploadReciept(expenseId,file);
         return ResponseEntity.ok("Reciept Uploaded ");
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Resource> getReceipt(@PathVariable Long id) throws IOException {


        User user=getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Path filePath = Paths.get(expense.getReceiptUrl());
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/recurring")
    public ResponseEntity<String> addRecurring(@RequestBody RecurringExpenseRequestDTO recurringExpenseRequestDTO) {
        recurringExpenseService.createRecurring(recurringExpenseRequestDTO);
        return ResponseEntity.ok("Recurring expense added");
    }
}
