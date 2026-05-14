package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.validator.ValidExpenseRows;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ValidExpenseRows
public class ExpenseFormDto {
    private Long projectId;
    private String vehicle;
    private Integer engineCc;
    private String plate;
    private Integer attachmentCount;
    private String notes;
    private List<ExpenseRowDto> rows = new ArrayList<>();
}