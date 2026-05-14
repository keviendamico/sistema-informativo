package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.UserRole;
import com.rextart.sys.sistemainformativo.model.validator.PasswordRequiredOnCreate;
import com.rextart.sys.sistemainformativo.model.validator.UniqueEmail;
import com.rextart.sys.sistemainformativo.model.validator.UniqueUsername;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@UniqueUsername
@UniqueEmail
@PasswordRequiredOnCreate
public class UserFormDto {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean active = true;
    private String password;
    private List<Long> projectIds = new ArrayList<>();
}