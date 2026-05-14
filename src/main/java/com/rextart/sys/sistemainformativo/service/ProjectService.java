package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.Project;
import com.rextart.sys.sistemainformativo.model.dto.ProjectFormDto;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAllOrdered();
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    public ProjectFormDto toForm(Project p) {
        ProjectFormDto form = new ProjectFormDto();
        form.setProjectId(p.getId());
        form.setCode(p.getCode());
        form.setDescription(p.getDescription());
        form.setActive(p.isActive());
        form.setAbsence(p.isAbsence());
        form.setInternal(p.isInternal());
        return form;
    }

    @Transactional
    public void create(ProjectFormDto form) {
        Project p = new Project();
        p.setCode(form.getCode().trim().toUpperCase());
        p.setDescription(form.getDescription());
        p.setActive(form.isActive());
        p.setAbsence(form.isAbsence());
        p.setInternal(form.isInternal());
        projectRepository.save(p);
        log.info("Project created: code='{}'", p.getCode());
    }

    @Transactional
    public void update(Long id, ProjectFormDto form) {
        Project p = getById(id);
        p.setDescription(form.getDescription());
        p.setActive(form.isActive());
        p.setAbsence(form.isAbsence());
        p.setInternal(form.isInternal());
        projectRepository.save(p);
        log.info("Project updated: id={}, code='{}'", id, p.getCode());
    }
}