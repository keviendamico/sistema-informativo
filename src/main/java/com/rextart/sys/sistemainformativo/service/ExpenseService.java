package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.ExpenseReport;
import com.rextart.sys.sistemainformativo.model.ExpenseRow;
import com.rextart.sys.sistemainformativo.model.ExpenseStatus;
import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.model.dto.ExpenseFormDto;
import com.rextart.sys.sistemainformativo.model.dto.ExpenseRowDto;
import com.rextart.sys.sistemainformativo.repository.ExpenseReportRepository;
import com.rextart.sys.sistemainformativo.repository.ExpenseRowRepository;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseReportRepository expenseReportRepository;
    private final ExpenseRowRepository expenseRowRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ExpenseReport> getExpensesForUser(UserDetails principal) {
        User user = loadUser(principal);
        return expenseReportRepository.findByUserOrderByYearDescMonthDesc(user);
    }

    public List<ExpenseReport> getAllExpenses() {
        return expenseReportRepository.findAllByOrderByYearDescMonthDesc();
    }

    public ExpenseReport getById(Long id, UserDetails principal) {
        ExpenseReport report = expenseReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense report not found"));
        checkOwnership(report, principal);
        return report;
    }

    @Transactional
    public ExpenseReport create(UserDetails principal) {
        User user = loadUser(principal);
        LocalDate now = LocalDate.now();

        if (expenseReportRepository.findByUserAndYearAndMonth(user, now.getYear(), now.getMonthValue()).isPresent()) {
            throw new IllegalStateException("An expense report already exists for the current month.");
        }

        ExpenseReport report = new ExpenseReport();
        report.setUser(user);
        report.setYear(now.getYear());
        report.setMonth(now.getMonthValue());
        report.setStatus(ExpenseStatus.DRAFT);
        ExpenseReport saved = expenseReportRepository.save(report);
        log.info("Expense report created: id={}, user='{}', period={}/{}", saved.getId(), user.getUsername(), now.getMonthValue(), now.getYear());
        return saved;
    }

    public ExpenseFormDto buildEditForm(ExpenseReport report) {
        ExpenseFormDto form = new ExpenseFormDto();
        form.setProjectId(report.getProject() != null ? report.getProject().getId() : null);
        form.setVehicle(report.getVehicle());
        form.setEngineCc(report.getEngineCc());
        form.setPlate(report.getPlate());
        form.setAttachmentCount(report.getAttachmentCount());
        form.setNotes(report.getNotes());

        List<ExpenseRow> rows = expenseRowRepository.findByExpenseReportId(report.getId());
        List<ExpenseRowDto> rowDtos = new ArrayList<>();
        for (ExpenseRow row : rows) {
            ExpenseRowDto dto = new ExpenseRowDto();
            dto.setDay(row.getDay());
            dto.setKm(row.getKm());
            dto.setRoute(row.getRoute());
            dto.setRoundTrip(row.isRoundTrip());
            dto.setVehicleType(row.getVehicleType());
            dto.setMealAmount(row.getMealAmount());
            dto.setAccommodationAmount(row.getAccommodationAmount());
            dto.setOtherAmount(row.getOtherAmount());
            dto.setDescription(row.getDescription());
            dto.setPaymentMethod(row.getPaymentMethod());
            rowDtos.add(dto);
        }
        form.setRows(rowDtos);
        return form;
    }

    @Transactional
    public void saveRows(Long reportId, ExpenseFormDto form, UserDetails principal) {
        ExpenseReport report = expenseReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Expense report not found"));
        checkOwnership(report, principal);

        report.setProject(form.getProjectId() != null
                ? projectRepository.getReferenceById(form.getProjectId()) : null);
        report.setVehicle(form.getVehicle());
        report.setEngineCc(form.getEngineCc());
        report.setPlate(form.getPlate());
        report.setAttachmentCount(form.getAttachmentCount() != null ? form.getAttachmentCount() : 0);
        report.setNotes(form.getNotes());

        expenseRowRepository.deleteByExpenseReportId(reportId);
        expenseRowRepository.flush();

        List<ExpenseRow> toSave = new ArrayList<>();
        for (ExpenseRowDto dto : form.getRows()) {
            ExpenseRow row = new ExpenseRow();
            row.setExpenseReport(report);
            row.setDay(dto.getDay());
            row.setKm(dto.getKm());
            row.setRoute(dto.getRoute());
            row.setRoundTrip(dto.isRoundTrip());
            row.setVehicleType(dto.getVehicleType());
            row.setMealAmount(dto.getMealAmount());
            row.setAccommodationAmount(dto.getAccommodationAmount());
            row.setOtherAmount(dto.getOtherAmount());
            row.setDescription(dto.getDescription());
            row.setPaymentMethod(dto.getPaymentMethod());
            toSave.add(row);
        }
        expenseRowRepository.saveAll(toSave);
        expenseReportRepository.save(report);
        log.info("Expense report {} saved: {} rows", reportId, toSave.size());
    }

    @Transactional
    public void submit(Long reportId, UserDetails principal) {
        ExpenseReport report = expenseReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Expense report not found"));
        checkOwnership(report, principal);

        if (report.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT expense reports can be submitted.");
        }
        report.setStatus(ExpenseStatus.PENDING);
        report.setSubmittedAt(LocalDateTime.now());
        expenseReportRepository.save(report);
        log.info("Expense report {} submitted by user '{}'", reportId, principal.getUsername());
    }

    @Transactional
    public void approve(Long reportId, UserDetails principal) {
        ExpenseReport report = expenseReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Expense report not found"));

        if (report.getStatus() != ExpenseStatus.PENDING) {
            throw new IllegalStateException("Only PENDING expense reports can be approved.");
        }
        User approver = loadUser(principal);
        report.setStatus(ExpenseStatus.APPROVED);
        report.setValidatedBy(approver);
        report.setValidatedAt(LocalDateTime.now());
        expenseReportRepository.save(report);
        log.info("Expense report {} approved by '{}'", reportId, principal.getUsername());
    }

    private void checkOwnership(ExpenseReport report, UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin && !principal.getUsername().equals(report.getUser().getUsername())) {
            log.warn("Access denied: user '{}' attempted to access expense report {}", principal.getUsername(), report.getId());
            throw new SecurityException("Access denied to this expense report.");
        }
    }

    private User loadUser(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}