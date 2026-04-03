package com.alok.Expense_Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ExpenseRequestDTO {

    private LocalDateTime expenseDate;
    private BigDecimal amount;
    private Long categoryId;
    private String note;



}
