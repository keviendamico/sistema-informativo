package com.rextart.sys.sistemainformativo.model.dto;

import com.rextart.sys.sistemainformativo.model.UserRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserFormDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean active = true;
    private String password;
    private List<Long> projectIds = new ArrayList<>();
}