package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.Repository.CategoryRepository;
import com.alok.Expense_Tracker.Repository.ExpenseRepository;
import com.alok.Expense_Tracker.Repository.RecurringExpenseRepository;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.Security.RolePermissionMapping;
import com.alok.Expense_Tracker.dto.CategoryRequestDto;
import com.alok.Expense_Tracker.dto.CategoryResponseDto;
import com.alok.Expense_Tracker.entity.*;
import com.alok.Expense_Tracker.entity.type.CategoryType;
import com.alok.Expense_Tracker.entity.type.PermissionType;
import com.alok.Expense_Tracker.entity.type.RoleType;
import com.alok.Expense_Tracker.service.CategoryService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
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
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {
        User user=getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.CATEGORY_CREATE)) {
            throw new RuntimeException("Not allowed");
        }

        boolean categoryExists =
                categoryRepository.existsByNameIgnoreCaseAndUser(
                        categoryRequestDto.getName(),
                        user
                );

        if (categoryExists) {
            throw new IllegalArgumentException(
                    "Category already exists for this user"
            );
        }
        Category category=Category.builder()
                .categoryType(categoryRequestDto.getCategoryType())
                .createdAt(LocalDateTime.now())
                .name(categoryRequestDto.getName())
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        categoryRepository.save(category);
        return modelMapper.map(category,CategoryResponseDto.class);

    }

    @Override
    public List<CategoryResponseDto> findAllCategories() {
        User user=getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.CATEGORY_READ)) {
            throw new RuntimeException("Not allowed");
        }
        List<Category> categories;
        if(user.getRoles().contains(RoleType.ADMIN)){
            categories= categoryRepository.findAll();
        }else{
            categories= categoryRepository.findByUser(user);
        }
        return categories.stream().map(category -> modelMapper.map(category,CategoryResponseDto.class)).toList();

    }

    @Override
    public CategoryResponseDto updateCategory(Long id,CategoryRequestDto categoryRequesteDto) {
        User user=getCurrentUser();
        if(!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.CATEGORY_UPDATE)){
            throw new RuntimeException("Not allowed");
        }
        Category category=categoryRepository.findById(id).orElseThrow(()->new RuntimeException("Category does not exist for this User id"+user.getId()));
        if(!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN) ){
            throw new RuntimeException("Unautherized access");
        }
        category.setCategoryType(categoryRequesteDto.getCategoryType());
        category.setName(categoryRequesteDto.getName());

        Category updatedCategory=categoryRepository.save(category);
        return modelMapper.map(updatedCategory,CategoryResponseDto.class);
    }

    @Override
    public void deleteCategories(Long id) {
        User user=getCurrentUser();
        if(!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.CATEGORY_DELETE)){
            throw new RuntimeException("Not allowed");
        }
        Category category=categoryRepository.findById(id).orElseThrow(()->new RuntimeException("Category with this id does not exists"));
        if(!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)){
            throw new RuntimeException("Unautherized access");
        }
        categoryRepository.delete(category);

    }

    @Override
    public List<CategoryResponseDto> findUserandCategoryType(CategoryType type) {
        User user=getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.CATEGORY_READ)) {
            throw new RuntimeException("Not allowed");
        }
        List<Category> categories;
        if(user.getRoles().contains(RoleType.ADMIN)){
            categories=categoryRepository.findByCategoryType(type);
        }else{
            categories=categoryRepository.findByUserAndCategoryType(user,type);
        }


        return  categories.stream().map(category->modelMapper.map(category,CategoryResponseDto.class)).toList();

    }

    @Override
    public CategoryResponseDto findAllCategoriesById(Long id) {
        User user=getCurrentUser();
        if (!RolePermissionMapping.hasPermission(user.getRoles(), PermissionType.CATEGORY_READ)) {
            throw new RuntimeException("Not allowed");
        }
        Category category=categoryRepository.findById(id).orElseThrow(()->new RuntimeException("Category with this id does not exists"));
        if(!category.getUser().getId().equals(user.getId()) && !user.getRoles().contains(RoleType.ADMIN)){
            throw new RuntimeException("Unautherized access");
        }
        return modelMapper.map(category,CategoryResponseDto.class);

    }

    @Service
    public static class RecurringExpenseScheduler {

        private RecurringExpenseRepository recurringExpenseRepository;

        private ExpenseRepository expenseRepository;

        @Scheduled(cron = "0 0 0 * * ?")
        public void processRecurringExpenses() {

            LocalDateTime today = LocalDateTime.now();

            List<RecurringExpense> list =
                    recurringExpenseRepository.findByNextExecutionDate(today);

            for (RecurringExpense re : list) {

                Expense expense = new Expense();
                expense.setAmount(re.getAmount());
                expense.setCategory(re.getCategory());
                expense.setUser(re.getUser());
                expense.setExpenseDate(today);

                expenseRepository.save(expense);


                if (re.getFrequency().equals("MONTHLY")) {
                    re.setNextExecutionDate(today.plusMonths(1));
                } else if (re.getFrequency().equals("WEEKLY")) {
                    re.setNextExecutionDate(today.plusWeeks(1));
                }

                recurringExpenseRepository.save(re);
            }
        }
    }
}
