package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.ProjectColumnDto;
import com.rextart.sys.sistemainformativo.model.dto.TimesheetFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NoDuplicateProjectsValidator implements ConstraintValidator<NoDuplicateProjects, TimesheetFormDto> {

    @Override
    public boolean isValid(TimesheetFormDto form, ConstraintValidatorContext context) {
        if (form == null) return true;
        Set<Long> seen = new HashSet<>();
        return form.getColumns().stream()
                .map(ProjectColumnDto::getProjectId)
                .filter(Objects::nonNull)
                .allMatch(seen::add);
    }
}
