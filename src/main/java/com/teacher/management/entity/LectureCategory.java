package com.teacher.management.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureCategory {
    MARKETING("마케팅"),
    IT("IT"),
    SEO("SEO");

    private final String description;
}
