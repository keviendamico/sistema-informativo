package com.rextart.sys.sistemainformativo.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordRequiredOnCreateValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordRequiredOnCreate {
    String message() default "La password è obbligatoria per un nuovo utente.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}