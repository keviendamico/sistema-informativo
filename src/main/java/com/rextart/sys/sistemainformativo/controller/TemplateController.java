package com.rextart.sys.sistemainformativo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TemplateController {

    @GetMapping("/templates")
    public String list(Model model) {
        model.addAttribute("pageTitle", "Template");
        model.addAttribute("activePage", "templates");
        return "template/list";
    }
}