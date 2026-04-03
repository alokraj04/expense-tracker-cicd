package com.alok.Expense_Tracker.Repository;

import com.alok.Expense_Tracker.entity.type.AuthProviderType;
import com.alok.Expense_Tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByProviderTypeAndProviderId(AuthProviderType providerType, String providerId);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
