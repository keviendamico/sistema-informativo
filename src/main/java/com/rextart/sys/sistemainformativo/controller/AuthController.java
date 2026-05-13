package com.rextart.sys.sistemainformativo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class AuthController {

    @GetMapping({"/", "/login"})
    public String loginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("Already authenticated user '{}' redirected to /timesheet", authentication.getName());
            return "redirect:/timesheet";
        }
        log.debug("Serving login page");
        return "auth/login";
    }
}
