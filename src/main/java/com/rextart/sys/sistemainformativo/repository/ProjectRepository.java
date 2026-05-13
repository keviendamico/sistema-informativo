package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByActiveTrue();
}