package com.rextart.sys.sistemainformativo.controller;

import com.rextart.sys.sistemainformativo.model.DayType;
import com.rextart.sys.sistemainformativo.model.Timesheet;
import com.rextart.sys.sistemainformativo.model.TimesheetStatus;
import com.rextart.sys.sistemainformativo.model.dto.TimesheetFormDto;
import com.rextart.sys.sistemainformativo.repository.AbsenceTypeRepository;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.service.TimesheetService;
import com.rextart.sys.sistemainformativo.util.ItalianHolidayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;
    private final ProjectRepository projectRepository;
    private final AbsenceTypeRepository absenceTypeRepository;

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        List<Timesheet> timesheets = isAdmin ? timesheetService.getAllTimesheets()
                                 : timesheetService.getTimesheetsForUser(principal);

        log.debug("Timesheet list loaded for user '{}' (admin={}): {} records",
                principal.getUsername(), isAdmin, timesheets.size());

        model.addAttribute("timesheets", timesheets);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("pageTitle", "Timesheet");
        model.addAttribute("activePage", "timesheet");
        return "timesheet/list";
    }

    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        try {
            timesheetService.create(principal);
            log.info("Timesheet created for user '{}'", principal.getUsername());
            ra.addFlashAttribute("success", "Timesheet creato con successo.");
        } catch (IllegalStateException e) {
            log.warn("Duplicate timesheet creation attempt by user '{}'", principal.getUsername());
            ra.addFlashAttribute("error", "Esiste già un timesheet per il mese corrente.");
        }
        return "redirect:/timesheet";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        Timesheet ts = timesheetService.getById(id, principal);
        if (ts.getStatus() != TimesheetStatus.DRAFT) {
            return "redirect:/timesheet/" + id + "/detail";
        }

        TimesheetFormDto form = timesheetService.buildEditForm(ts);

        YearMonth ym = YearMonth.of(ts.getYear(), ts.getMonth());
        List<String> rowClassList = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            DayType dt = ItalianHolidayUtil.getDayType(LocalDate.of(ts.getYear(), ts.getMonth(), d));
            rowClassList.add(switch (dt) {
                case SATURDAY -> "ts-row-saturday";
                case HOLIDAY  -> "ts-row-holiday";
                default       -> "";
            });
        }

        model.addAttribute("timesheet", ts);
        model.addAttribute("form", form);
        model.addAttribute("rowClassList", rowClassList);
        model.addAttribute("projects", projectRepository.findByActiveTrue());
        model.addAttribute("absenceTypes", absenceTypeRepository.findAllByOrderByCodeAsc());
        model.addAttribute("daysInMonth", ym.lengthOfMonth());
        model.addAttribute("pageTitle", "Modifica Timesheet");
        model.addAttribute("activePage", "timesheet");
        log.debug("Edit form loaded for timesheet {}", id);
        return "timesheet/edit";
    }

    @PostMapping("/{id}/edit")
    public String save(@PathVariable Long id,
                       @Valid @ModelAttribute("form") TimesheetFormDto form,
                       BindingResult result,
                       @AuthenticationPrincipal UserDetails principal,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            String message = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .distinct()
                    .findFirst()
                    .orElse("Dati non validi.");
            log.warn("Validation failed for timesheet {} by user '{}': {}", id, principal.getUsername(), message);
            ra.addFlashAttribute("error", message);
            return "redirect:/timesheet/" + id + "/edit";
        }
        try {
            timesheetService.saveRows(id, form, principal);
            log.info("Timesheet {} saved by user '{}'", id, principal.getUsername());
            ra.addFlashAttribute("success", "Timesheet salvato con successo.");
        } catch (Exception e) {
            log.warn("Save failed for timesheet {} by user '{}': {}", id, principal.getUsername(), e.getMessage());
            ra.addFlashAttribute("error", "Errore durante il salvataggio.");
        }
        return "redirect:/timesheet";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        Timesheet ts = timesheetService.getById(id, principal);
        model.addAttribute("timesheet", ts);
        model.addAttribute("pageTitle", "Dettaglio Timesheet");
        model.addAttribute("activePage", "timesheet");
        return "timesheet/detail";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        try {
            timesheetService.submit(id, principal);
            log.info("Timesheet {} submitted by user '{}'", id, principal.getUsername());
            ra.addFlashAttribute("success", "Timesheet inviato in validazione.");
        } catch (Exception e) {
            log.warn("Submit failed for timesheet {} by user '{}': {}", id, principal.getUsername(), e.getMessage());
            ra.addFlashAttribute("error", "Operazione non consentita.");
        }
        return "redirect:/timesheet";
    }
}
