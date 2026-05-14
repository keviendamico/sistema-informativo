package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
    List<DocumentTemplate> findAllByOrderByDisplayNameAsc();
}