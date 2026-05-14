package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.ExpenseReport;
import com.rextart.sys.sistemainformativo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {

    List<ExpenseReport> findByUserOrderByYearDescMonthDesc(User user);

    List<ExpenseReport> findAllByOrderByYearDescMonthDesc();

    Optional<ExpenseReport> findByUserAndYearAndMonth(User user, int year, int month);
}