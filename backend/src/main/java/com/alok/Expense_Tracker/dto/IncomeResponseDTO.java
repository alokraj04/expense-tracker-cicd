package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.CategoryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeResponseDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime incomeDate;
    private String description;
    private String categoryName;



}
