package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByCode(String code);

    @Query("""
            SELECT p FROM Project p
            ORDER BY
              CASE WHEN p.absence = false AND p.internal = false THEN 0
                   WHEN p.internal = true THEN 1
                   ELSE 2
              END ASC,
              p.code ASC
            """)
    List<Project> findAllOrdered();

    Optional<Project> findByCode(String code);

    // Absence dropdown in timesheet (absence=true, includes REXCCAS/REXCCFO)
    List<Project> findByAbsenceTrueAndActiveTrueOrderByCodeAsc();

    // Expense report project dropdown (internal=true)
    List<Project> findByInternalTrueAndActiveTrueOrderByCodeAsc();

    // User form multiselect: exclude pure absence types (absence=true AND internal=false)
    @Query("SELECT p FROM Project p WHERE p.active = true AND NOT (p.absence = true AND p.internal = false) ORDER BY p.code ASC")
    List<Project> findAssignable();
}
