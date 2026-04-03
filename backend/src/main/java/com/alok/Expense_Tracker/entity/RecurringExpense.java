package com.alok.Expense_Tracker.entity;

import com.alok.Expense_Tracker.entity.type.Frequency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
public class RecurringExpense {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private BigDecimal amount;

    @ManyToOne
    private Category category;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private LocalDateTime nextExecutionDate;
}
