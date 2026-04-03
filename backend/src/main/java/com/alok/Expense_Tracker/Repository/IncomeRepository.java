package com.alok.Expense_Tracker.Repository;

import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.dto.IncomeRequestDTO;
import com.alok.Expense_Tracker.dto.IncomeResponseDTO;
import com.alok.Expense_Tracker.entity.Income;
import com.alok.Expense_Tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {


     List<Income> findByUser(User user);

     Optional<Income> findById(Long id);

     @Query("""
    SELECT new com.alok.Expense_Tracker.dto.CategorySummaryDto(
        i.category.name,
        SUM(i.amount),
        COUNT(i)
    )
    FROM Income i
    WHERE i.user = :user
    GROUP BY i.category.name
""")
     List<CategorySummaryDto> getIncomeSummary(User user);

     @Query("""
    SELECT COALESCE(SUM(i.amount), 0)
    FROM Income i
    WHERE i.user = :user
""")

     BigDecimal sumIncomeByUser(User user);
     @Query("""
    SELECT FUNCTION('TO_CHAR', i.incomeDate, 'YYYY-MM'),
           SUM(i.amount)
    FROM Income i
    WHERE i.user.id = :userId
    GROUP BY FUNCTION('TO_CHAR', i.incomeDate, 'YYYY-MM')
""")
     List<Object[]> getMonthlyIncomeRaw(Long userId);


}
