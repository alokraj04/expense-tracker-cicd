package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.Exception.GlobalExceptionHandler;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.dto.*;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.type.AuthProviderType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserProfileResponseDTO getProfile(Long userId) {
        User user = findUserById(userId);
        return mapToProfileDTO(user);
    }

    public UserProfileResponseDTO updateProfile(Long userId, UpdateProfileDto dto) {
        User user = findUserById(userId);
        user.setName(dto.getName());
        userRepository.save(user);
        return mapToProfileDTO(user);
    }

    public String requestEmailChange(Long userId, String newEmail) {
        User user = findUserById(userId);

        if (user.getProviderType() != AuthProviderType.EMAIL) {
            throw new IllegalStateException(
                    "Email change is not available for "
                            + user.getProviderType().name() + " accounts"
            );
        }

        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("New email must be different from current email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use by another account");
        }

        String token = UUID.randomUUID().toString();
        user.setPendingEmail(newEmail);
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendEmailVerificationEmail(newEmail, user.getName(), token);

        return "Verification link sent to " + newEmail + ". Please verify to confirm the change.";
    }

    public String confirmEmailChange(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            // clear expired token
            user.setEmailVerificationToken(null);
            user.setEmailVerificationExpiry(null);
            user.setPendingEmail(null);
            userRepository.save(user);
            throw new IllegalStateException("Verification link expired. Please request a new one.");
        }

        if (user.getPendingEmail() == null) {
            throw new IllegalStateException("No pending email change found");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        return "Email updated successfully";
    }

    public String changePassword(Long userId, ChangePasswordDto dto) {
        User user = findUserById(userId);

        if (user.getProviderType() != AuthProviderType.EMAIL) {
            throw new IllegalStateException(
                    "Password change is not available for "
                            + user.getProviderType().name() + " accounts"
            );
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // security notification
        emailService.sendPasswordChangedNotificationEmail(user.getEmail(), user.getName());

        return "Password changed successfully";
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));

        if (user.getProviderType() != AuthProviderType.EMAIL) {
            throw new IllegalStateException(
                    "This account uses " + user.getProviderType().name()
                            + " login. Password reset is not available."
            );
        }

        if (user.getLastPasswordResetRequest() != null &&
                user.getLastPasswordResetRequest().isAfter(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalStateException(
                    "Please wait 5 minutes before requesting another password reset"
            );
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        user.setLastPasswordResetRequest(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);

        return "Password reset link sent to " + email;
    }

    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            // clear expired token
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new IllegalStateException("Reset token has expired. Please request a new one.");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // prevent reusing same password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setLastPasswordResetRequest(null);
        userRepository.save(user);

        emailService.sendPasswordChangedNotificationEmail(user.getEmail(), user.getName());

        return "Password reset successful. Please login with your new password.";
    }

    public String deleteAccount(Long userId, String password) {
        User user = findUserById(userId);

        if (user.getProviderType() == AuthProviderType.EMAIL) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Please provide your password to confirm deletion");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("Incorrect password");
            }
        }

        userRepository.delete(user);
        return "Account deleted successfully";
    }

    public List<UserProfileResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToProfileDTO)
                .collect(Collectors.toList());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private UserProfileResponseDTO mapToProfileDTO(User user) {
        return new UserProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getProviderType()
        );
    }
}