package com.rextart.sys.sistemainformativo.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidExpenseRowsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpenseRows {
    String message() default "Tutte le righe devono avere il giorno compilato.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}