package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.ProjectFormDto;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueProjectCodeValidator implements ConstraintValidator<UniqueProjectCode, ProjectFormDto> {

    private final ProjectRepository projectRepository;

    @Override
    public boolean isValid(ProjectFormDto form, ConstraintValidatorContext ctx) {
        if (form.getCode() == null || form.getCode().isBlank()) return true;
        return projectRepository.findByCode(form.getCode())
                .map(p -> p.getId().equals(form.getProjectId()))
                .orElse(true);
    }
}