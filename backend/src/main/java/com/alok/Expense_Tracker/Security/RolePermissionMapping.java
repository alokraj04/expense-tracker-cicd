package com.alok.Expense_Tracker.Security;

import com.alok.Expense_Tracker.entity.type.PermissionType;
import com.alok.Expense_Tracker.entity.type.RoleType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RolePermissionMapping {
    private static final Map<RoleType, Set<PermissionType>> map = Map.of(

            RoleType.ADMIN, Set.of(
                    PermissionType.EXPENSE_READ,   PermissionType.EXPENSE_CREATE,
                    PermissionType.EXPENSE_UPDATE, PermissionType.EXPENSE_DELETE,
                    PermissionType.INCOME_READ,    PermissionType.INCOME_CREATE,
                    PermissionType.INCOME_UPDATE,  PermissionType.INCOME_DELETE,
                    PermissionType.BUDGET_READ,    PermissionType.BUDGET_CREATE,
                    PermissionType.BUDGET_UPDATE,  PermissionType.BUDGET_DELETE,
                    PermissionType.CATEGORY_READ,  PermissionType.CATEGORY_CREATE,
                    PermissionType.CATEGORY_UPDATE,PermissionType.CATEGORY_DELETE,
                    PermissionType.USER_READ,      PermissionType.USER_MANAGE
            ),

            RoleType.USER, Set.of(
                    PermissionType.EXPENSE_READ,   PermissionType.EXPENSE_CREATE,
                    PermissionType.EXPENSE_UPDATE, PermissionType.EXPENSE_DELETE,
                    PermissionType.INCOME_READ,    PermissionType.INCOME_CREATE,
                    PermissionType.INCOME_UPDATE,  PermissionType.INCOME_DELETE,
                    PermissionType.BUDGET_READ,    PermissionType.BUDGET_CREATE,
                    PermissionType.BUDGET_UPDATE,  PermissionType.BUDGET_DELETE,
                    PermissionType.CATEGORY_READ,  PermissionType.CATEGORY_CREATE,
                    PermissionType.CATEGORY_UPDATE,PermissionType.CATEGORY_DELETE
            ),

            RoleType.AUDITOR, Set.of(
                    PermissionType.EXPENSE_READ,
                    PermissionType.INCOME_READ,
                    PermissionType.BUDGET_READ,
                    PermissionType.CATEGORY_READ,
                    PermissionType.USER_READ
            )
    );

    public static Set<SimpleGrantedAuthority> getAuthoritiesForRole(RoleType role) {

        Set<SimpleGrantedAuthority> authorities = map.getOrDefault(role, Set.of())
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        return authorities;
    }

    public static boolean hasPermission(Set<RoleType> roles, PermissionType permission) {

        return roles.stream()
                .anyMatch(role -> map.getOrDefault(role, Set.of()).contains(permission));
    }
    }

