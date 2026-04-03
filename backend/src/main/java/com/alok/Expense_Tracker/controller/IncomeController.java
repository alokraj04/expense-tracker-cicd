package com.alok.Expense_Tracker.controller;

import com.alok.Expense_Tracker.Exception.BadRequestException;
import com.alok.Expense_Tracker.Exception.ResourceNotFoundException;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.IncomeRequestDTO;
import com.alok.Expense_Tracker.dto.IncomeResponseDTO;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeResponseDTO> addIncome(@Valid @RequestBody IncomeRequestDTO incomeRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.addIncome(incomeRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponseDTO>> getAllIncomes() {
        return ResponseEntity.ok(incomeService.getAllIncome());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> updateIncome(@PathVariable Long id, @Valid @RequestBody IncomeRequestDTO incomeRequestDTO) {
        return ResponseEntity.ok(incomeService.updateIncome(id, incomeRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CategorySummaryDto>> getSummary() {
        return ResponseEntity.ok(incomeService.getSummary());
    }
}
