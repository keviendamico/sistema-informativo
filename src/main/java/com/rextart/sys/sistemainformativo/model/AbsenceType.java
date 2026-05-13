package com.rextart.sys.sistemainformativo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "absence_types")
public class AbsenceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private String description;
}