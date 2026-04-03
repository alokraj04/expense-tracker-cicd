package com.alok.Expense_Tracker.Repository;

import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.type.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    boolean existsByNameIgnoreCaseAndUser(String name, User user);

    List<Category> findByUser(User user);

    List<Category> findByUserAndCategoryType(User user, CategoryType categoryType);

    List<Category> findByCategoryType(CategoryType type);
}
