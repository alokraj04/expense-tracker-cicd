package com.alok.Expense_Tracker.controller;

import com.alok.Expense_Tracker.dto.CategoryBreakdownResponse;
import com.alok.Expense_Tracker.dto.IncomeExpenseDto;
import com.alok.Expense_Tracker.dto.MonthlySummaryDto;
import com.alok.Expense_Tracker.dto.WeeklySummaryDto;
import com.alok.Expense_Tracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/category-breakdown")
    public ResponseEntity<CategoryBreakdownResponse>  getCategoryBreakdown() {
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown());
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySummaryDto>> getMonthlySummary() {
        return ResponseEntity.ok(analyticsService.getMonthlySummary());
    }
    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklySummaryDto>> getWeeklySummary() {
        return ResponseEntity.ok(analyticsService.getWeeklySummary());
    }

    @GetMapping("/income-expense")
    public ResponseEntity<List<IncomeExpenseDto>> getIncomeVsExpense() {
        return ResponseEntity.ok(analyticsService.getIncomeVsExpense());
    }
    @GetMapping("/report/excel")
    public ResponseEntity<InputStreamResource> downloadExcel() throws IOException {

        ByteArrayInputStream file = analyticsService.generateExcelReport();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=expenses.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(file));
    }
}
