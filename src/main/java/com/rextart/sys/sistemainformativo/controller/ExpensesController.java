package com.rextart.sys.sistemainformativo.controller;

import com.rextart.sys.sistemainformativo.model.ExpenseReport;
import com.rextart.sys.sistemainformativo.model.ExpenseStatus;
import com.rextart.sys.sistemainformativo.model.PaymentMethod;
import com.rextart.sys.sistemainformativo.model.VehicleType;
import com.rextart.sys.sistemainformativo.model.dto.ExpenseFormDto;
import com.rextart.sys.sistemainformativo.model.dto.ExpenseRowDto;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.service.ExpensePdfService;
import com.rextart.sys.sistemainformativo.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpensesController {

    private final ExpenseService expenseService;
    private final ExpensePdfService expensePdfService;
    private final ProjectRepository projectRepository;

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        boolean isAdmin = isAdmin(principal);
        List<ExpenseReport> reports = isAdmin
                ? expenseService.getAllExpenses()
                : expenseService.getExpensesForUser(principal);

        model.addAttribute("reports", reports);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("pageTitle", "Note spese");
        model.addAttribute("activePage", "expenses");
        return "expenses/list";
    }

    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails principal, RedirectAttributes ra) {
        try {
            expenseService.create(principal);
            ra.addFlashAttribute("success", "Nota spese creata con successo.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", "Esiste già una nota spese per il mese corrente.");
        }
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        ExpenseReport report = expenseService.getById(id, principal);
        if (report.getStatus() != ExpenseStatus.DRAFT) {
            return "redirect:/expenses/" + id + "/detail";
        }
        return buildEditView(report, expenseService.buildEditForm(report), model);
    }

    @PostMapping(value = "/{id}/edit", params = "addRow")
    public String addRow(@PathVariable Long id,
                         @ModelAttribute("form") ExpenseFormDto form,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        form.getRows().add(new ExpenseRowDto());
        ExpenseReport report = expenseService.getById(id, principal);
        return buildEditView(report, form, model);
    }

    @PostMapping(value = "/{id}/edit", params = "removeRow")
    public String removeRow(@PathVariable Long id,
                            @ModelAttribute("form") ExpenseFormDto form,
                            @RequestParam("removeRow") int index,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        if (index >= 0 && index < form.getRows().size()) {
            form.getRows().remove(index);
        }
        ExpenseReport report = expenseService.getById(id, principal);
        return buildEditView(report, form, model);
    }

    @PostMapping("/{id}/edit")
    public String save(@PathVariable Long id,
                       @Valid @ModelAttribute("form") ExpenseFormDto form,
                       BindingResult br,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model,
                       RedirectAttributes ra) {
        ExpenseReport report = expenseService.getById(id, principal);
        if (br.hasErrors()) {
            String message = br.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .distinct().findFirst().orElse("Dati non validi.");
            model.addAttribute("error", message);
            return buildEditView(report, form, model);
        }
        try {
            expenseService.saveRows(id, form, principal);
            ra.addFlashAttribute("success", "Nota spese salvata con successo.");
        } catch (Exception e) {
            log.warn("Save failed for expense report {} by user '{}': {}", id, principal.getUsername(), e.getMessage());
            ra.addFlashAttribute("error", "Errore durante il salvataggio.");
        }
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        ExpenseReport report = expenseService.getById(id, principal);
        ExpenseFormDto form = expenseService.buildEditForm(report);

        model.addAttribute("report", report);
        model.addAttribute("form", form);
        model.addAttribute("isAdmin", isAdmin(principal));
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("pageTitle", "Dettaglio Nota spese");
        model.addAttribute("activePage", "expenses");
        return "expenses/detail";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        try {
            expenseService.submit(id, principal);
            ra.addFlashAttribute("success", "Nota spese inviata in validazione.");
        } catch (Exception e) {
            log.warn("Submit failed for expense report {} by user '{}': {}", id, principal.getUsername(), e.getMessage());
            ra.addFlashAttribute("error", "Operazione non consentita.");
        }
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails principal) {
        ExpenseReport report = expenseService.getById(id, principal);
        byte[] pdf = expensePdfService.generatePdf(report);
        String filename = String.format("notaspese_%d_%02d_%s.pdf",
                report.getYear(), report.getMonth(), report.getUser().getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String buildEditView(ExpenseReport report, ExpenseFormDto form, Model model) {
        model.addAttribute("report", report);
        model.addAttribute("form", form);
        model.addAttribute("internalProjects", projectRepository.findByInternalTrueAndActiveTrueOrderByCodeAsc());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("pageTitle", "Modifica Nota spese");
        model.addAttribute("activePage", "expenses");
        return "expenses/edit";
    }

    private boolean isAdmin(UserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
