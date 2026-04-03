package com.alok.Expense_Tracker.controller;


import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.dto.*;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import com.alok.Expense_Tracker.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
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

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileDto dto) {

        User user = getCurrentUser();
        return ResponseEntity.ok(userService.updateProfile(user.getId(), dto));
    }

    @PutMapping("/request-email-change")
    public ResponseEntity<String> requestEmailChange(@RequestParam @Email @NotBlank String newEmail) {

        User user = getCurrentUser();
        return ResponseEntity.ok(userService.requestEmailChange(user.getId(), newEmail));
    }

    @GetMapping("/confirm-email-change")
    public ResponseEntity<String> confirmEmailChange(@RequestParam String token) {
        return ResponseEntity.ok(userService.confirmEmailChange(token));
    }


    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordDto dto) {

        User user = getCurrentUser();
        return ResponseEntity.ok(userService.changePassword(user.getId(), dto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam @Email @NotBlank String email) {

        return ResponseEntity.ok(userService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        return ResponseEntity.ok(userService.resetPassword(token, newPassword));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@RequestParam(required = false) String password) {
        User user = getCurrentUser();
        return ResponseEntity.ok(userService.deleteAccount(user.getId(), password));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserProfileResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}

