package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.UserFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UserFormDto> {

    @Override
    public boolean isValid(UserFormDto form, ConstraintValidatorContext ctx) {
        if (!StringUtils.hasText(form.getPassword())) return true;
        return form.getPassword().equals(form.getConfirmPassword());
    }
}