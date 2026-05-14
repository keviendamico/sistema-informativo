package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.PaymentMethod;
import com.rextart.sys.sistemainformativo.model.VehicleType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRowDto {
    private Integer day;
    private BigDecimal km;
    private String route;
    private boolean roundTrip;
    private VehicleType vehicleType;
    private BigDecimal mealAmount;
    private BigDecimal accommodationAmount;
    private BigDecimal otherAmount;
    private String description;
    private PaymentMethod paymentMethod;
}