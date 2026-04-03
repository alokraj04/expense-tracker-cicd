package com.alok.Expense_Tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private BigDecimal amount;
    @ManyToOne
    @JoinColumn(name="category_id",nullable = false)
    private Category category;
    @ManyToOne
    @JoinColumn(name="user_id",nullable = false)
    @JsonIgnore
    private User user;
    private LocalDateTime expenseDate;
    private LocalDateTime updateAt;
    private LocalDateTime createdAt;
    @Column(length = 500)
    private String note;

    private String receiptUrl;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

}
