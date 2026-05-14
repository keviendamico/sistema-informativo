package com.rextart.sys.sistemainformativo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "expense_rows")
public class ExpenseRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_report_id")
    private ExpenseReport expenseReport;

    @Column(nullable = false)
    private int day;

    private BigDecimal km;

    private String route;

    @Column(name = "round_trip", nullable = false)
    private boolean roundTrip = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;

    @Column(name = "meal_amount")
    private BigDecimal mealAmount;

    @Column(name = "accommodation_amount")
    private BigDecimal accommodationAmount;

    @Column(name = "other_amount")
    private BigDecimal otherAmount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
}