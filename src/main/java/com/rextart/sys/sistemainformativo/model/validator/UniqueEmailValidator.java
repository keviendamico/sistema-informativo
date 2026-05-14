package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.UserFormDto;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, UserFormDto> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(UserFormDto form, ConstraintValidatorContext ctx) {
        if (form.getEmail() == null) return true;
        return userRepository.findByEmail(form.getEmail())
                .map(u -> u.getId().equals(form.getUserId()))
                .orElse(true);
    }
}