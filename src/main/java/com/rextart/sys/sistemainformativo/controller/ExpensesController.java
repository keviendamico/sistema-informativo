package com.rextart.sys.sistemainformativo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExpensesController {

    @GetMapping("/expenses")
    public String list(Model model) {
        model.addAttribute("pageTitle", "Note spese");
        model.addAttribute("activePage", "expenses");
        return "expenses/list";
    }
}