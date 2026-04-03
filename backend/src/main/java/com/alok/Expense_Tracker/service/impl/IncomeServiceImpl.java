package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.Exception.BadRequestException;
import com.alok.Expense_Tracker.Exception.ResourceNotFoundException;
import com.alok.Expense_Tracker.Exception.UnauthorizedException;
import com.alok.Expense_Tracker.Repository.CategoryRepository;
import com.alok.Expense_Tracker.Repository.IncomeRepository;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.Security.RolePermissionMapping;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.IncomeRequestDTO;
import com.alok.Expense_Tracker.dto.IncomeResponseDTO;
import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.Income;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import com.alok.Expense_Tracker.entity.type.CategoryType;
import com.alok.Expense_Tracker.entity.type.PermissionType;
import com.alok.Expense_Tracker.entity.type.RoleType;
import com.alok.Expense_Tracker.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrinciple userPrinciple) {
            return userPrinciple.getUser();
        }

        if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oauthUser) {

            String email = oauthUser.getAttribute("email");

            if (email == null) {
                throw new RuntimeException("Email not found from OAuth provider");
            }

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found in DB"));
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
    }

    @Override
    public IncomeResponseDTO addIncome(IncomeRequestDTO incomeRequestDTO) {
        User user = getCurrentUser();

        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.INCOME_CREATE)) {
            throw new RuntimeException("Not allowed");
        }

        Category category = categoryRepository.findById(incomeRequestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category does not exist"));

        if (!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You are not allowed to add this income");
        }
        if (!category.getCategoryType().equals(CategoryType.INCOME)) {
            throw new BadRequestException("Category must be a type of INCOME");
        }

        Income income = new Income();
        income.setAmount(incomeRequestDTO.getAmount());
        income.setIncomeDate(incomeRequestDTO.getIncomeDate());
        income.setDescription(incomeRequestDTO.getDescription());
        income.setCategory(category);
        income.setUser(user);
        income.setCreatedAt(LocalDateTime.now());

        incomeRepository.save(income);
        return modelMapper.map(income, IncomeResponseDTO.class);
    }

    @Override
    public List<IncomeResponseDTO> getAllIncome() {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.INCOME_READ)) {
            throw new RuntimeException("Not allowed");
        }

        List<Income> income;
        if (user.getRoles().contains(RoleType.ADMIN)) {
            income = incomeRepository.findAll();
        } else {
            income = incomeRepository.findByUser(user);
        }

        return income.stream().map(inc -> {
            IncomeResponseDTO dto = new IncomeResponseDTO();
            dto.setId(inc.getId());
            dto.setAmount(inc.getAmount());
            dto.setIncomeDate(inc.getIncomeDate());
            dto.setDescription(inc.getDescription());
            dto.setCategoryName(inc.getCategory().getName());
            return dto;
        }).toList();
    }

    @Override
    public IncomeResponseDTO updateIncome(Long id, IncomeRequestDTO incomeRequestDTO) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.INCOME_UPDATE)) {
            throw new RuntimeException("Not allowed");
        }

        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot update this income");
        }

        Category category = categoryRepository.findById(incomeRequestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot use this category");
        }
        if (category.getCategoryType() != CategoryType.INCOME) {
            throw new BadRequestException("Category must be of type INCOME");
        }

        income.setAmount(incomeRequestDTO.getAmount());
        income.setIncomeDate(incomeRequestDTO.getIncomeDate());
        income.setDescription(incomeRequestDTO.getDescription());
        income.setCategory(category);
        income.setUpdatedAt(LocalDateTime.now());

        incomeRepository.save(income);
        return modelMapper.map(income, IncomeResponseDTO.class);
    }

    @Override
    public void deleteIncome(Long id) {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.INCOME_DELETE)) {
            throw new RuntimeException("Not allowed");
        }

        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)) {
            throw new UnauthorizedException("You cannot delete this income");
        }
        incomeRepository.delete(income);
    }

    @Override
    public List<CategorySummaryDto> getSummary() {
        User user = getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.INCOME_READ)) {
            throw new RuntimeException("Not allowed");
        }
        if (user.getRoles().contains(RoleType.ADMIN)) {
            return incomeRepository.getIncomeSummary(null);
        }
        return incomeRepository.getIncomeSummary(user);
    }
}




