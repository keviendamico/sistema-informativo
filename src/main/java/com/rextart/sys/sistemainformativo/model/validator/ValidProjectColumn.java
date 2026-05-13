package com.rextart.sys.sistemainformativo.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProjectColumnValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProjectColumn {

    String message() default "Ogni colonna con ore inserite deve avere una commessa selezionata.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}