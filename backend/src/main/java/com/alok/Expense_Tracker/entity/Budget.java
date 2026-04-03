package com.alok.Expense_Tracker.entity;

import com.alok.Expense_Tracker.entity.type.BudgetPeriod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Category category;
    private BigDecimal budgetLimit;
    @Enumerated(EnumType.STRING)
    private BudgetPeriod period;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
