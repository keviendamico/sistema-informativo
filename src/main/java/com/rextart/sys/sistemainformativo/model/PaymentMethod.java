package com.rextart.sys.sistemainformativo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("Carta"),
    ADVANCE("Anticipo"),
    PREPAID("Prepagato");

    private final String label;
}