package com.creatio.autotest.testdata.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserCredentials {

    private final UserRole role;
    private final String   login;
    private final String   password;

    @Override
    public String toString() {
        return role.name() + "(" + login + ")";
    }
}