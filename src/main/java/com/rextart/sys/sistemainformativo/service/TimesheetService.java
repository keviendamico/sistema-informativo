package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.dto.AbsenceRowDto;
import com.rextart.sys.sistemainformativo.model.dto.ProjectColumnDto;
import com.rextart.sys.sistemainformativo.model.dto.TimesheetFormDto;
import com.rextart.sys.sistemainformativo.model.Timesheet;
import com.rextart.sys.sistemainformativo.model.TimesheetAbsenceRow;
import com.rextart.sys.sistemainformativo.model.TimesheetRow;
import com.rextart.sys.sistemainformativo.model.TimesheetStatus;
import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.repository.TimesheetAbsenceRowRepository;
import com.rextart.sys.sistemainformativo.repository.TimesheetRepository;
import com.rextart.sys.sistemainformativo.repository.TimesheetRowRepository;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetRowRepository timesheetRowRepository;
    private final TimesheetAbsenceRowRepository timesheetAbsenceRowRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<Timesheet> getTimesheetsForUser(UserDetails principal) {
        log.debug("Loading timesheets for user '{}'", principal.getUsername());
        User user = loadUser(principal);
        return timesheetRepository.findByUserOrderByYearDescMonthDesc(user);
    }

    public List<Timesheet> getAllTimesheets() {
        log.debug("Loading all timesheets (admin)");
        return timesheetRepository.findAllByOrderByYearDescMonthDesc();
    }

    public Timesheet getById(Long id, UserDetails principal) {
        log.debug("Loading timesheet {} for user '{}'", id, principal.getUsername());
        Timesheet ts = timesheetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));
        checkOwnership(ts, principal);
        return ts;
    }

    @Transactional
    public Timesheet create(UserDetails principal) {
        User user = loadUser(principal);
        LocalDate now = LocalDate.now();

        if (timesheetRepository.findByUserAndYearAndMonth(user, now.getYear(), now.getMonthValue()).isPresent()) {
            throw new IllegalStateException("A timesheet already exists for the current month.");
        }

        Timesheet ts = new Timesheet();
        ts.setUser(user);
        ts.setYear(now.getYear());
        ts.setMonth(now.getMonthValue());
        ts.setStatus(TimesheetStatus.DRAFT);
        Timesheet saved = timesheetRepository.save(ts);
        log.info("Timesheet created: id={}, user='{}', period={}/{}", saved.getId(), user.getUsername(), now.getMonthValue(), now.getYear());
        return saved;
    }

    public TimesheetFormDto buildEditForm(Timesheet ts) {
        List<TimesheetRow> existing = timesheetRowRepository.findByTimesheetId(ts.getId());

        // Map projectId → column index (preserving insertion order)
        Map<Long, Integer> projectToColumn = new LinkedHashMap<>();
        for (TimesheetRow row : existing) {
            Long pid = row.getProject().getId();
            if (!projectToColumn.containsKey(pid) && projectToColumn.size() < 10) {
                projectToColumn.put(pid, projectToColumn.size());
            }
        }

        TimesheetFormDto form = TimesheetFormDto.empty();
        form.setActivities(ts.getActivities());
        form.setNotes(ts.getNotes());

        // Assign projectIds to columns
        projectToColumn.forEach((pid, colIdx) ->
                form.getColumns().get(colIdx).setProjectId(pid));

        // Fill project hours
        for (TimesheetRow row : existing) {
            Long pid = row.getProject().getId();
            Integer colIdx = projectToColumn.get(pid);
            if (colIdx != null) {
                int dayIndex = row.getDay() - 1;
                form.getColumns().get(colIdx).getHours().set(dayIndex, row.getHours());
            }
        }

        // Fill absence rows
        List<TimesheetAbsenceRow> absenceRows = timesheetAbsenceRowRepository.findByTimesheetId(ts.getId());
        for (TimesheetAbsenceRow ar : absenceRows) {
            int dayIndex = ar.getDay() - 1;
            AbsenceRowDto dto = form.getAbsenceRows().get(dayIndex);
            dto.setHours(ar.getHours());
            if (ar.getProject() != null) {
                dto.setProjectId(ar.getProject().getId());
            }
        }

        return form;
    }

    @Transactional
    public void saveRows(Long timesheetId, TimesheetFormDto form, UserDetails principal) {
        log.debug("Saving rows for timesheet {} by user '{}'", timesheetId, principal.getUsername());
        Timesheet ts = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));
        checkOwnership(ts, principal);

        timesheetRowRepository.deleteByTimesheetId(timesheetId);
        timesheetRowRepository.flush();

        List<TimesheetRow> toSave = new ArrayList<>();
        for (ProjectColumnDto col : form.getColumns()) {
            if (col.getProjectId() == null) continue;
            for (int i = 0; i < col.getHours().size(); i++) {
                Integer h = col.getHours().get(i);
                if (h != null && h > 0) {
                    TimesheetRow row = new TimesheetRow();
                    row.setTimesheet(ts);
                    row.setProject(projectRepository.getReferenceById(col.getProjectId()));
                    row.setDay(i + 1);
                    row.setHours(h);
                    toSave.add(row);
                }
            }
        }

        timesheetRowRepository.saveAll(toSave);

        // Save absence rows
        timesheetAbsenceRowRepository.deleteByTimesheetId(timesheetId);
        timesheetAbsenceRowRepository.flush();

        List<TimesheetAbsenceRow> absenceToSave = new ArrayList<>();
        List<AbsenceRowDto> absenceDtos = form.getAbsenceRows();
        for (int i = 0; i < absenceDtos.size(); i++) {
            AbsenceRowDto dto = absenceDtos.get(i);
            boolean hasHours = dto.getHours() != null && dto.getHours() > 0;
            boolean hasType = dto.getProjectId() != null;
            if (hasHours || hasType) {
                TimesheetAbsenceRow ar = new TimesheetAbsenceRow();
                ar.setTimesheet(ts);
                ar.setDay(i + 1);
                ar.setHours(hasHours ? dto.getHours() : null);
                if (hasType) {
                    ar.setProject(projectRepository.getReferenceById(dto.getProjectId()));
                }
                absenceToSave.add(ar);
            }
        }
        timesheetAbsenceRowRepository.saveAll(absenceToSave);

        ts.setActivities(form.getActivities());
        ts.setNotes(form.getNotes());
        timesheetRepository.save(ts);
        log.info("Timesheet {} saved: {} project rows, {} absence rows", timesheetId, toSave.size(), absenceToSave.size());
    }

    @Transactional
    public void submit(Long timesheetId, UserDetails principal) {
        Timesheet ts = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));
        checkOwnership(ts, principal);

        if (ts.getStatus() != TimesheetStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT timesheets can be submitted.");
        }
        ts.setStatus(TimesheetStatus.PENDING);
        ts.setSubmittedAt(java.time.LocalDateTime.now());
        timesheetRepository.save(ts);
        log.info("Timesheet {} submitted by user '{}'", timesheetId, principal.getUsername());
    }

    @Transactional
    public void approve(Long timesheetId, UserDetails principal) {
        Timesheet ts = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));

        if (ts.getStatus() != TimesheetStatus.PENDING) {
            throw new IllegalStateException("Only PENDING timesheets can be approved.");
        }
        User approver = loadUser(principal);
        ts.setStatus(TimesheetStatus.APPROVED);
        ts.setValidatedBy(approver);
        ts.setValidatedAt(java.time.LocalDateTime.now());
        timesheetRepository.save(ts);
        log.info("Timesheet {} approved by '{}'", timesheetId, principal.getUsername());
    }

    private void checkOwnership(Timesheet ts, UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin && !principal.getUsername().equals(ts.getUser().getUsername())) {
            log.warn("Access denied: user '{}' attempted to access timesheet {}", principal.getUsername(), ts.getId());
            throw new SecurityException("Access denied to this timesheet.");
        }
    }

    private User loadUser(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
