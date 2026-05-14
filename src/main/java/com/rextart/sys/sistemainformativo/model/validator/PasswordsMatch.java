package com.rextart.sys.sistemainformativo.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {
    String message() default "Le password non coincidono.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}