package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.CategoryType;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDto {

    private String name;
    private CategoryType categoryType;


}
