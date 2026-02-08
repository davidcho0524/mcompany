package com.teacher.management.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatus {
    NEW("신규"),
    RECONTACT_1("1회차 재섭외"),
    RECONTACT_2("2회차 재섭외");

    private final String description;
}
