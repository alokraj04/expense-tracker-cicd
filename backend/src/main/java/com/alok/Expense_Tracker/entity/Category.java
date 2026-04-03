package com.alok.Expense_Tracker.entity;

import com.alok.Expense_Tracker.entity.type.CategoryType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;
    private String name;
    @ManyToOne
    @JsonIgnore
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
