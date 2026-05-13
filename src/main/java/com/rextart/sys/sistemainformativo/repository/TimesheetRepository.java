package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.Timesheet;
import com.rextart.sys.sistemainformativo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByUserOrderByYearDescMonthDesc(User user);

    List<Timesheet> findAllByOrderByYearDescMonthDesc();

    Optional<Timesheet> findByUserAndYearAndMonth(User user, int year, int month);
}