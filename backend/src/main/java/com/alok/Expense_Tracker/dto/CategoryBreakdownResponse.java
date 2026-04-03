package com.alok.Expense_Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBreakdownResponse {
    private BigDecimal totalAmount;
    private List<CategoryBreakdownDto> categories;


}
