package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.Project;
import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.model.dto.UserFormDto;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public void create(UserFormDto form) {
        if (!StringUtils.hasText(form.getPassword())) {
            throw new IllegalArgumentException("La password è obbligatoria per un nuovo utente.");
        }
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + form.getUsername() + "' già in uso.");
        }
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + form.getEmail() + "' già in uso.");
        }

        User user = new User();
        applyForm(user, form);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        log.info("User '{}' created with role {}", user.getUsername(), user.getRole());
    }

    @Transactional
    public void update(Long id, UserFormDto form) {
        User user = getById(id);

        userRepository.findByEmail(form.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new IllegalArgumentException("Email '" + form.getEmail() + "' already used."); });

        applyForm(user, form);
        if (StringUtils.hasText(form.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
            log.info("Password updated for user '{}'", user.getUsername());
        }
        userRepository.save(user);
        log.info("User '{}' updated", user.getUsername());
    }

    public UserFormDto toForm(User user) {
        UserFormDto form = new UserFormDto();
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setRole(user.getRole());
        form.setActive(user.isActive());
        form.setProjectIds(user.getProjects().stream().map(Project::getId).toList());
        return form;
    }

    private void applyForm(User user, UserFormDto form) {
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setRole(form.getRole());
        user.setActive(form.isActive());

        List<Project> projects = form.getProjectIds() == null
                ? List.of()
                : projectRepository.findAllById(form.getProjectIds());
        user.setProjects(projects);
    }
}
