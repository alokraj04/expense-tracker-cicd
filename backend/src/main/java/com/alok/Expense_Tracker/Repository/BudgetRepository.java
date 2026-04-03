package com.alok.Expense_Tracker.Repository;

import com.alok.Expense_Tracker.dto.BudgetRequestDto;
import com.alok.Expense_Tracker.entity.Budget;
import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.type.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserAndCategoryAndPeriod(User user, Category category, BudgetPeriod budgetPeriod);


    List<Budget> findByUser(User user);

    @Query("""
    SELECT SUM(b.budgetLimit)
    FROM Budget b
    WHERE b.user.id = :userId
""")
    BigDecimal getTotalBudget(Long userId);
}
