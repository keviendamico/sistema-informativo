package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.ExpenseRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRowRepository extends JpaRepository<ExpenseRow, Long> {

    List<ExpenseRow> findByExpenseReportId(Long expenseReportId);

    void deleteByExpenseReportId(Long expenseReportId);
}