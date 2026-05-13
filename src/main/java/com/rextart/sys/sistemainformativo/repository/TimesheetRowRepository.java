package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.TimesheetRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetRowRepository extends JpaRepository<TimesheetRow, Long> {

    List<TimesheetRow> findByTimesheetId(Long timesheetId);

    void deleteByTimesheetId(Long timesheetId);
}