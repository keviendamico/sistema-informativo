package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.validator.ValidProjectColumn;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@ValidProjectColumn
public class ProjectColumnDto {
    private Long projectId;
    private List<Integer> hours = new ArrayList<>(Collections.nCopies(31, null));
}