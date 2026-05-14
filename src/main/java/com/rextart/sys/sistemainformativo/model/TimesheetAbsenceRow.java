package com.rextart.sys.sistemainformativo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "timesheet_absence_rows",
       uniqueConstraints = @UniqueConstraint(columnNames = {"timesheet_id", "day"}))
public class TimesheetAbsenceRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id")
    private Timesheet timesheet;

    @Column(nullable = false)
    private int day;

    private Integer hours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
}