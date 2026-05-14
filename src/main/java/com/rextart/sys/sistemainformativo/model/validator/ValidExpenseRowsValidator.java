package com.rextart.sys.sistemainformativo.model.validator;

import com.rextart.sys.sistemainformativo.model.dto.ExpenseFormDto;
import com.rextart.sys.sistemainformativo.model.dto.ExpenseRowDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidExpenseRowsValidator implements ConstraintValidator<ValidExpenseRows, ExpenseFormDto> {

    @Override
    public boolean isValid(ExpenseFormDto form, ConstraintValidatorContext ctx) {
        if (form.getRows() == null) return true;
        for (ExpenseRowDto row : form.getRows()) {
            if (row.getDay() == null) return false;
        }
        return true;
    }
}