package com.rextart.sys.sistemainformativo.controller;

import com.rextart.sys.sistemainformativo.model.DayType;
import com.rextart.sys.sistemainformativo.model.Project;
import com.rextart.sys.sistemainformativo.model.Timesheet;
import com.rextart.sys.sistemainformativo.model.TimesheetStatus;
import com.rextart.sys.sistemainformativo.model.dto.ProjectColumnDto;
import com.rextart.sys.sistemainformativo.model.dto.TimesheetFormDto;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.service.TimesheetPdfService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;
    private final TimesheetPdfService timesheetPdfService;
    private final ProjectRepository projectRepository;

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

        List<Project> userProjects = ts.getUser().getProjects().stream()
                .filter(Project::isActive)
                .sorted(Comparator.comparing(Project::getCode))
                .toList();

        model.addAttribute("timesheet", ts);
        model.addAttribute("form", form);
        model.addAttribute("rowClassList", buildRowClassList(ts.getYear(), ts.getMonth(), ym.lengthOfMonth()));
        model.addAttribute("projects", userProjects);
        model.addAttribute("absenceProjects", projectRepository.findByAbsenceTrueAndActiveTrueOrderByCodeAsc());
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
        TimesheetFormDto form = timesheetService.buildEditForm(ts);

        YearMonth ym = YearMonth.of(ts.getYear(), ts.getMonth());
        int daysInMonth = ym.lengthOfMonth();

        List<ProjectColumnDto> activeColumns = form.getColumns();

        List<String> columnCodes = activeColumns.stream()
                .map(c -> c.getProjectId() == null ? "—"
                        : projectRepository.findById(c.getProjectId())
                                .map(Project::getCode)
                                .orElse("?"))
                .toList();

        Map<Long, String> absenceCodeMap = projectRepository.findByAbsenceTrueAndActiveTrueOrderByCodeAsc().stream()
                .collect(Collectors.toMap(Project::getId, Project::getCode));

        List<Integer> columnTotals = activeColumns.stream()
                .map(c -> c.getHours().stream()
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum())
                .toList();

        List<Integer> rowTotals = new ArrayList<>();
        int absenceTotal = 0;
        for (int i = 0; i < daysInMonth; i++) {
            int sum = 0;
            for (ProjectColumnDto col : activeColumns) {
                Integer h = col.getHours().get(i);
                if (h != null) sum += h;
            }
            Integer absH = form.getAbsenceRows().get(i).getHours();
            if (absH != null) {
                sum += absH;
                absenceTotal += absH;
            }
            rowTotals.add(sum);
        }
        int grandTotal = columnTotals.stream().mapToInt(Integer::intValue).sum() + absenceTotal;

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        model.addAttribute("timesheet", ts);
        model.addAttribute("form", form);
        model.addAttribute("activeColumns", activeColumns);
        model.addAttribute("columnCodes", columnCodes);
        model.addAttribute("absenceCodeMap", absenceCodeMap);
        model.addAttribute("rowClassList", buildRowClassList(ts.getYear(), ts.getMonth(), daysInMonth));
        model.addAttribute("columnTotals", columnTotals);
        model.addAttribute("rowTotals", rowTotals);
        model.addAttribute("absenceTotal", absenceTotal);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("pageTitle", "Dettaglio Timesheet");
        model.addAttribute("activePage", "timesheet");
        log.debug("Detail loaded for timesheet {}", id);
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

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails principal) {
        Timesheet ts = timesheetService.getById(id, principal);
        byte[] pdf = timesheetPdfService.generatePdf(ts);
        String filename = String.format("timesheet_%d_%02d_%s.pdf",
                ts.getYear(), ts.getMonth(), ts.getUser().getUsername());
        log.info("Requested PDF download for timesheet {} from '{}'", id, principal.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private List<String> buildRowClassList(int year, int month, int daysInMonth) {
        List<String> list = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            DayType dt = ItalianHolidayUtil.getDayType(LocalDate.of(year, month, d));
            list.add(switch (dt) {
                case SATURDAY -> "ts-row-saturday";
                case HOLIDAY  -> "ts-row-holiday";
                default       -> "";
            });
        }
        return list;
    }
}
