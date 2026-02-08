package com.teacher.management.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompanyType {
    LARGE("대기업"),
    MEDIUM("중견기업"),
    SMALL("중소기업"),
    PRIVATE("개인"),
    PUBLIC("공공기관");

    private final String description;
}
