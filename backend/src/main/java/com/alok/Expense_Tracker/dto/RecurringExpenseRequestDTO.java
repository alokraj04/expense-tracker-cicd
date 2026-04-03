package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.Frequency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurringExpenseRequestDTO {
    private BigDecimal amount;
    private Long categoryId;
    private Frequency frequency;
    private LocalDateTime startDate;
}
