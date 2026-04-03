package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.Exception.BadRequestException;
import com.alok.Expense_Tracker.Exception.ResourceNotFoundException;
import com.alok.Expense_Tracker.Repository.IncomeRepository;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.IncomeRequestDTO;
import com.alok.Expense_Tracker.dto.IncomeResponseDTO;
import com.alok.Expense_Tracker.entity.Income;
import com.alok.Expense_Tracker.entity.User;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeService {

    IncomeResponseDTO addIncome(IncomeRequestDTO incomeRequestDTO) ;

    List<IncomeResponseDTO> getAllIncome();

    IncomeResponseDTO updateIncome(Long id, IncomeRequestDTO incomeRequestDTO) ;

    void deleteIncome(Long id);


    List<CategorySummaryDto> getSummary();
}
