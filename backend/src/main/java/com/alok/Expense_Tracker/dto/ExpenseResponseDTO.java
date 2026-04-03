package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.Income;
import com.alok.Expense_Tracker.entity.User;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponseDTO {


        private Long id;

        private BigDecimal amount;

        private Long categoryId;
        private String categoryName;

        private LocalDateTime expenseDate;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private String note;
        private String receiptUrl;
    }



