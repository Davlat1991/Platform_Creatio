package com.creatio.autotest.testdata.models;

public enum UserRole {

    MANAGER     ("user.manager"),
    UNDERWRITER ("user.underwriter"),
    IKOK        ("user.ikok"),
    IKOK_GO     ("user.ikok_go"),
    CASHIER     ("user.cashier");

    public final String configPrefix;

    UserRole(String configPrefix) {
        this.configPrefix = configPrefix;
    }
}