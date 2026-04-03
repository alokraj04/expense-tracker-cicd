package com.alok.Expense_Tracker.dto;

import com.alok.Expense_Tracker.entity.type.AuthProviderType;
import com.alok.Expense_Tracker.entity.type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Set<RoleType> roles;
    private AuthProviderType providerType;
}
