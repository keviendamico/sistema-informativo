package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.ProjectColumnDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProjectColumnValidator implements ConstraintValidator<ValidProjectColumn, ProjectColumnDto> {

    @Override
    public boolean isValid(ProjectColumnDto col, ConstraintValidatorContext context) {
        if (col == null) return true;
        boolean hasHours = col.getHours().stream().anyMatch(h -> h != null && h > 0);
        return !hasHours || col.getProjectId() != null;
    }
}