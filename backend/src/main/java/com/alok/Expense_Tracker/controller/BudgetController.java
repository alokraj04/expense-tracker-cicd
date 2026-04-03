package com.alok.Expense_Tracker.controller;

import com.alok.Expense_Tracker.dto.BudgetRequestDto;
import com.alok.Expense_Tracker.dto.BudgetResponseDto;
import com.alok.Expense_Tracker.dto.DashboardResponseDto;
import com.alok.Expense_Tracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponseDto> createBudget(@Valid @RequestBody BudgetRequestDto budgetRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(budgetRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDto> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetRequestDto budgetRequestDto) {
        return ResponseEntity.ok(budgetService.updateBudget(id, budgetRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDto>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudget());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getDashboard() {
        return ResponseEntity.ok(budgetService.getDashboard());
    }
}
