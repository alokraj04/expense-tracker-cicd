package com.alok.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
public class Income {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime incomeDate;
    private String description;
    private BigDecimal amount;
    @ManyToOne
    private Category category;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
