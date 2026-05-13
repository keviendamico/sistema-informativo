package com.rextart.sys.sistemainformativo.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoDuplicateProjectsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoDuplicateProjects {

    String message() default "La stessa commessa non può essere selezionata in più colonne.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}