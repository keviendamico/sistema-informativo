package com.rextart.sys.sistemainformativo.controller;

import com.rextart.sys.sistemainformativo.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TimesheetService timesheetService;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Utenti");
        model.addAttribute("activePage", "admin-users");
        return "admin/users";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("pageTitle", "Commesse");
        model.addAttribute("activePage", "admin-projects");
        return "admin/projects";
    }

    @PostMapping("/timesheet/{id}/approve")
    public String approve(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails principal,
                          RedirectAttributes ra) {
        try {
            timesheetService.approve(id, principal);
            log.info("Timesheet {} approved by '{}'", id, principal.getUsername());
            ra.addFlashAttribute("success", "Timesheet approvato.");
        } catch (Exception e) {
            log.warn("Approve failed for timesheet {}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", "Operazione non consentita.");
        }
        return "redirect:/timesheet";
    }
}