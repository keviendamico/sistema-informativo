package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.validator.NoDuplicateProjects;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@NoDuplicateProjects
public class TimesheetFormDto {
    private String activities;
    private String notes;

    @Valid
    private List<ProjectColumnDto> columns = new ArrayList<>();
    private List<AbsenceRowDto> absenceRows = new ArrayList<>();

    public static TimesheetFormDto empty() {
        TimesheetFormDto form = new TimesheetFormDto();
        for (int i = 0; i < 10; i++) {
            form.getColumns().add(new ProjectColumnDto());
        }
        for (int i = 0; i < 31; i++) {
            form.getAbsenceRows().add(new AbsenceRowDto());
        }
        return form;
    }
}
