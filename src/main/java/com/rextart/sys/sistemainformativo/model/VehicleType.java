package com.rextart.sys.sistemainformativo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {
    CAR("Auto"),
    MOTORCYCLE("Moto");

    private final String label;
}