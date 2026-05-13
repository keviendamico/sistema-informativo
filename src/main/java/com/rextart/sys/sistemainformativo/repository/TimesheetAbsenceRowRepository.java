package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.TimesheetAbsenceRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetAbsenceRowRepository extends JpaRepository<TimesheetAbsenceRow, Long> {

    List<TimesheetAbsenceRow> findByTimesheetId(Long timesheetId);

    void deleteByTimesheetId(Long timesheetId);
}