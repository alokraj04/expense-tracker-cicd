package com.alok.Expense_Tracker.Repository;

import com.alok.Expense_Tracker.dto.CategoryBreakdownDto;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.MonthlySummaryDto;
import com.alok.Expense_Tracker.dto.WeeklySummaryDto;
import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.Expense;
import com.alok.Expense_Tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {


   List<Expense> findByUser(User user);

   List<Expense> findByUserAndExpenseDateBetween(
           User user,
           LocalDateTime startDate,
           LocalDateTime endDate
   );

   List<Expense> findByUserAndCategory_Id(User user, Long categoryId);


   List<Expense> findByExpenseDateBetween(LocalDateTime start, LocalDateTime end
   );

   List<Expense> findByCategory_Id(Long categoryId);


   @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user = :user
    """)
   BigDecimal sumExpenseByUser(@Param("user") User user);

   @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user = :user
          AND e.category = :category
          AND e.expenseDate BETWEEN :start AND :end
    """)
   BigDecimal sumByUserAndCategoryAndDateBetween(
           @Param("user") User user,
           @Param("category") Category category,
           @Param("start") LocalDateTime start,
           @Param("end") LocalDateTime end
   );

   @Query("""
        SELECT SUM(e.amount)
        FROM Expense e
        WHERE e.user.id = :userId
    """)
   BigDecimal getTotalExpense(@Param("userId") Long userId);


   @Query("""
        SELECT new com.alok.Expense_Tracker.dto.CategorySummaryDto(
            c.name,
            SUM(e.amount),
            COUNT(e)
        )
        FROM Expense e
        JOIN e.category c
        WHERE e.user = :user
        GROUP BY c.name
    """)
   List<CategorySummaryDto> getCategorySummary(@Param("user") User user);

   @Query("""
        SELECT new com.alok.Expense_Tracker.dto.CategorySummaryDto(
            c.name,
            SUM(e.amount),
            COUNT(e)
        )
        FROM Expense e
        JOIN e.category c
        GROUP BY c.name
    """)
   List<CategorySummaryDto> getCategorySummaryForAll();

   @Query("""
        SELECT new com.alok.Expense_Tracker.dto.CategoryBreakdownDto(
            e.category.name,
            SUM(e.amount)
        )
        FROM Expense e
        WHERE e.user.id = :userId
        GROUP BY e.category.name
        ORDER BY SUM(e.amount) DESC
    """)
   List<CategoryBreakdownDto> getCategoryBreakdown(@Param("userId") Long userId);


   @Query("""
    SELECT new com.alok.Expense_Tracker.dto.MonthlySummaryDto(
        CONCAT(YEAR(e.expenseDate), '-', MONTH(e.expenseDate)),
        SUM(e.amount)
    )
    FROM Expense e
    WHERE e.user.id = :userId
    GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate)
    ORDER BY YEAR(e.expenseDate), MONTH(e.expenseDate)
""")
   List<MonthlySummaryDto> getMonthlySummary(@Param("userId") Long userId);


   @Query(value = """
    SELECT 
        EXTRACT(YEAR FROM e.expense_date) || '-W' || EXTRACT(WEEK FROM e.expense_date) AS week,
        SUM(e.amount) AS total
    FROM expense e
    WHERE e.user_id = :userId
    GROUP BY 
        EXTRACT(YEAR FROM e.expense_date),
        EXTRACT(WEEK FROM e.expense_date)
    ORDER BY 
        EXTRACT(YEAR FROM e.expense_date),
        EXTRACT(WEEK FROM e.expense_date)
""", nativeQuery = true)
   List<Object[]> getWeeklySummary(@Param("userId") Long userId);


   @Query(value = """
    SELECT TO_CHAR(expense_date, 'YYYY-MM') AS month,
           SUM(amount) AS total
    FROM expense
    WHERE user_id = :userId
    GROUP BY month
    ORDER BY month
""", nativeQuery = true)
   List<Object[]> getMonthlyExpenseRaw(@Param("userId") Long userId);
}