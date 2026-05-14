package com.rextart.sys.sistemainformativo.config;

import com.rextart.sys.sistemainformativo.model.Project;
import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.model.UserRole;
import com.rextart.sys.sistemainformativo.repository.ProjectRepository;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private record ProjectSeed(String code, String description, boolean absence, boolean internal) {}

    private static final List<ProjectSeed> SEED_PROJECTS = List.of(
            new ProjectSeed("104",     "Legge 104",                                             true,  false),
            new ProjectSeed("ALT",     "Allattamento",                                          true,  false),
            new ProjectSeed("ASP",     "Aspettativa",                                           true,  false),
            new ProjectSeed("CIG",     "Cassa integrazione",                                    true,  false),
            new ProjectSeed("CMT",     "Congedo matrimoniale",                                  true,  false),
            new ProjectSeed("CPA",     "Congedo parentale",                                     true,  false),
            new ProjectSeed("FER",     "Ferie",                                                 true,  false),
            new ProjectSeed("INF",     "Infortunio",                                            true,  false),
            new ProjectSeed("MAF",     "Maternità facoltativa al 30%",                          true,  false),
            new ProjectSeed("MAL",     "Malattia",                                              true,  false),
            new ProjectSeed("MAT",     "Maternità",                                             true,  false),
            new ProjectSeed("PDS",     "Permesso donazione sangue",                             true,  false),
            new ProjectSeed("PEF",     "Permesso ex-festività",                                 true,  false),
            new ProjectSeed("PEL",     "Permesso elettorale",                                   true,  false),
            new ProjectSeed("PGM",     "Permesso gravi motivi familiari",                       true,  false),
            new ProjectSeed("PLT",     "Permesso per lutto familiare",                          true,  false),
            new ProjectSeed("PMB",     "Permesso malattia bambino",                             true,  false),
            new ProjectSeed("PRM",     "Permesso retribuito maternità",                         true,  false),
            new ProjectSeed("PST",     "Permesso per motivi di studio",                         true,  false),
            new ProjectSeed("RCO",     "Riposo compensativo",                                   true,  false),
            new ProjectSeed("ROL",     "Permesso riduzione orario di lavoro",                   true,  false),
            new ProjectSeed("SCP",     "Sciopero",                                              true,  false),
            new ProjectSeed("REXCCAS", "Centro di Costo Rextart per Attività in Sede",         true,  true),
            new ProjectSeed("REXCCFO", "Centro di Costo Rextart per Formazione del Personale", true,  true)
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;

    @Value("${password.admin}")
    private String password;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        seedAdminUser();
        seedProjects();
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@rextart.com");
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setFirstName("Admin");
            admin.setLastName("Rextart");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("DataInitializer: admin user created");
        }
    }

    private void seedProjects() {
        int inserted = 0;
        for (ProjectSeed seed : SEED_PROJECTS) {
            if (!projectRepository.existsByCode(seed.code())) {
                Project p = new Project();
                p.setCode(seed.code());
                p.setDescription(seed.description());
                p.setAbsence(seed.absence());
                p.setInternal(seed.internal());
                p.setActive(true);
                projectRepository.save(p);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("DataInitializer: inserted {} projects", inserted);
        }
    }
}