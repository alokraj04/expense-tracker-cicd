package com.alok.Expense_Tracker.entity.type;

public enum RoleType {
    ADMIN, //has access to control the system and manage expenses
    USER, //has access to manage his own expenses
    AUDITOR, //read-only access given to manage expenses of other people

}
