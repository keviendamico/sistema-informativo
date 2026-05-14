package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.validator.UniqueProjectCode;
import lombok.Data;

@Data
@UniqueProjectCode
public class ProjectFormDto {
    private Long projectId;
    private String code;
    private String description;
    private boolean active = true;
    private boolean absence = false;
    private boolean internal = false;
}