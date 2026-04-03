package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.CategoryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeRequestDTO {
    @NotNull(message = "Amount is required")
    @Positive(message="Amount cannot be negative")
    private BigDecimal amount;
    @NotNull(message = "CategoryId cannot be eympty")
    private Long categoryId;
    private String description;
    @NotNull(message = "Income date is required")
    private LocalDateTime incomeDate;



}
