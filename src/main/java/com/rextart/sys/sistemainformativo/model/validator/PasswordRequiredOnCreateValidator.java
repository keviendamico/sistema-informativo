package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.UserFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class PasswordRequiredOnCreateValidator implements ConstraintValidator<PasswordRequiredOnCreate, UserFormDto> {

    @Override
    public boolean isValid(UserFormDto form, ConstraintValidatorContext ctx) {
        if (form.getUserId() != null) return true;
        return StringUtils.hasText(form.getPassword());
    }
}
