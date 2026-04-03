package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {

    private Long id;
    private CategoryType type;
    private String name;
    private LocalDateTime createdAt;


}
