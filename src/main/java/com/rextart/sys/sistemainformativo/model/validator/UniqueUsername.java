package com.rextart.sys.sistemainformativo.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {
    String message() default "Username già in uso.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}