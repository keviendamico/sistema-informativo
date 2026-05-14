package com.rextart.sys.sistemainformativo.controller;

import com.rextart.sys.sistemainformativo.model.dto.ProjectFormDto;
import com.rextart.sys.sistemainformativo.model.dto.UserFormDto;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.service.DocumentTemplateService;
import com.rextart.sys.sistemainformativo.service.ProjectService;
import com.rextart.sys.sistemainformativo.service.ExpenseService;
import com.rextart.sys.sistemainformativo.service.TimesheetService;
import com.rextart.sys.sistemainformativo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TimesheetService timesheetService;
    private final ExpenseService expenseService;
    private final DocumentTemplateService documentTemplateService;
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("pageTitle", "Utenti");
        model.addAttribute("activePage", "admin-users");
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("form", new UserFormDto());
        model.addAttribute("allProjects", projectRepository.findAssignable());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Nuovo utente");
        model.addAttribute("activePage", "admin-users");
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute("form") UserFormDto form,
                             BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().stream()
                    .findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Errore di validazione."));
            return "redirect:/admin/users/new";
        }
        userService.create(form);
        ra.addFlashAttribute("success", "Utente creato con successo.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        UserFormDto form = userService.toForm(userService.getById(id));
        form.setUserId(id);
        model.addAttribute("form", form);
        model.addAttribute("userId", id);
        model.addAttribute("allProjects", projectRepository.findAssignable());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Modifica utente");
        model.addAttribute("activePage", "admin-users");
        return "admin/user-form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("form") UserFormDto form,
                             BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().stream()
                    .findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Errore di validazione."));
            return "redirect:/admin/users/" + id + "/edit";
        }
        userService.update(id, form);
        ra.addFlashAttribute("success", "Utente aggiornato.");
        return "redirect:/admin/users";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", projectService.findAll());
        model.addAttribute("pageTitle", "Commesse");
        model.addAttribute("activePage", "admin-projects");
        return "admin/projects";
    }

    @GetMapping("/projects/new")
    public String newProjectForm(Model model) {
        model.addAttribute("form", new ProjectFormDto());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Nuova commessa");
        model.addAttribute("activePage", "admin-projects");
        return "admin/project-form";
    }

    @PostMapping("/projects")
    public String createProject(@Valid @ModelAttribute("form") ProjectFormDto form,
                                BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().stream()
                    .findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Errore di validazione."));
            return "redirect:/admin/projects/new";
        }
        projectService.create(form);
        ra.addFlashAttribute("success", "Commessa creata con successo.");
        return "redirect:/admin/projects";
    }

    @GetMapping("/projects/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        ProjectFormDto form = projectService.toForm(projectService.getById(id));
        model.addAttribute("form", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Modifica commessa");
        model.addAttribute("activePage", "admin-projects");
        return "admin/project-form";
    }

    @PostMapping("/projects/{id}")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("form") ProjectFormDto form,
                                BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().stream()
                    .findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Errore di validazione."));
            return "redirect:/admin/projects/" + id + "/edit";
        }
        projectService.update(id, form);
        ra.addFlashAttribute("success", "Commessa aggiornata.");
        return "redirect:/admin/projects";
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

    @PostMapping("/expense/{id}/approve")
    public String approveExpense(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes ra) {
        try {
            expenseService.approve(id, principal);
            log.info("Expense report {} approved by '{}'", id, principal.getUsername());
            ra.addFlashAttribute("success", "Nota spese approvata.");
        } catch (Exception e) {
            log.warn("Approve failed for expense report {}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", "Operazione non consentita.");
        }
        return "redirect:/expenses";
    }

    @PostMapping("/templates")
    public String uploadTemplate(@RequestParam("file") MultipartFile file,
                                 @RequestParam("displayName") String displayName,
                                 RedirectAttributes ra) {
        try {
            documentTemplateService.upload(file, displayName);
            ra.addFlashAttribute("success", "Template caricato con successo.");
        } catch (Exception e) {
            log.warn("Template upload failed: {}", e.getMessage());
            ra.addFlashAttribute("error", "Errore durante il caricamento: " + e.getMessage());
        }
        return "redirect:/templates";
    }

    @PostMapping("/templates/{id}/rename")
    public String renameTemplate(@PathVariable Long id,
                                 @RequestParam("displayName") String displayName,
                                 RedirectAttributes ra) {
        try {
            documentTemplateService.rename(id, displayName);
            ra.addFlashAttribute("success", "Template rinominato.");
        } catch (Exception e) {
            log.warn("Template rename failed for {}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", "Errore durante la rinomina.");
        }
        return "redirect:/templates";
    }

    @PostMapping("/templates/{id}/delete")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            documentTemplateService.delete(id);
            ra.addFlashAttribute("success", "Template eliminato.");
        } catch (Exception e) {
            log.warn("Template delete failed for {}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", "Errore durante l'eliminazione.");
        }
        return "redirect:/templates";
    }
}