package com.alok.Expense_Tracker.entity.type;

import lombok.*;

@RequiredArgsConstructor
@Getter
public enum PermissionType {
    EXPENSE_READ("expense:read"),
    EXPENSE_CREATE("expense:create"),
    EXPENSE_UPDATE("expense:update"),
    EXPENSE_DELETE("expense:delete"),

    INCOME_READ("income:read"),
    INCOME_CREATE("income:create"),
    INCOME_UPDATE("income:update"),
    INCOME_DELETE("income:delete"),

    BUDGET_READ("budget:read"),
    BUDGET_CREATE("budget:create"),
    BUDGET_UPDATE("budget:update"),
    BUDGET_DELETE("budget:delete"),

    CATEGORY_READ("category:read"),
    CATEGORY_CREATE("category:create"),
    CATEGORY_DELETE("category:delete"),
    CATEGORY_UPDATE("category:update"),

    USER_READ("user:read"),
    USER_MANAGE("user:manage");

    public final String permission;

}
