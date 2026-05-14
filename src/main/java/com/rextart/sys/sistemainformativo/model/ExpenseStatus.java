package com.rextart.sys.sistemainformativo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpenseStatus {
    DRAFT("Bozza"),
    PENDING("In validazione"),
    APPROVED("Approvato");

    private final String label;
}