package com.rextart.sys.sistemainformativo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "expense_reports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year", "month"}))
public class ExpenseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String vehicle;

    @Column(name = "engine_cc")
    private Integer engineCc;

    private String plate;

    @Column(name = "attachment_count", nullable = false)
    private int attachmentCount = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    private LocalDateTime submittedAt;
    private LocalDateTime validatedAt;

    @OneToMany(mappedBy = "expenseReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseRow> rows = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}