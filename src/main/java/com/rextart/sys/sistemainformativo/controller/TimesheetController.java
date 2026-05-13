package com.rextart.sys.sistemainformativo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TimesheetController {

    @GetMapping("/timesheet")
    public String list() {
        return "timesheet/list";
    }
}