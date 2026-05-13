package com.rextart.sys.sistemainformativo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "timesheet_rows")
public class TimesheetRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id")
    private Timesheet timesheet;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private int day;

    @Column(nullable = false)
    private int hours = 0;
}