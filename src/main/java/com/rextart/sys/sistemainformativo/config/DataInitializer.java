package com.rextart.sys.sistemainformativo.config;

import com.rextart.sys.sistemainformativo.model.AbsenceType;
import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.model.UserRole;
import com.rextart.sys.sistemainformativo.repository.AbsenceTypeRepository;
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

    private static final List<String[]> ABSENCE_TYPES = List.of(
            new String[]{"104",     "Legge 104"},
            new String[]{"ALT",     "Allattamento"},
            new String[]{"ASP",     "Aspettativa"},
            new String[]{"CIG",     "Cassa integrazione"},
            new String[]{"CMT",     "Congedo matrimoniale"},
            new String[]{"CPA",     "Congedo parentale"},
            new String[]{"FER",     "Ferie"},
            new String[]{"INF",     "Infortunio"},
            new String[]{"MAF",     "Maternità facoltativa al 30%"},
            new String[]{"MAL",     "Malattia"},
            new String[]{"MAT",     "Maternità"},
            new String[]{"PDS",     "Permesso donazione sangue"},
            new String[]{"PEF",     "Permesso ex-festività"},
            new String[]{"PEL",     "Permesso elettorale"},
            new String[]{"PGM",     "Permesso gravi motivi familiari"},
            new String[]{"PLT",     "Permesso per lutto familiare"},
            new String[]{"PMB",     "Permesso malattia bambino"},
            new String[]{"PRM",     "Permesso retribuito maternità"},
            new String[]{"PST",     "Permesso per motivi di studio"},
            new String[]{"RCO",     "Riposo compensativo"},
            new String[]{"ROL",     "Permesso riduzione orario di lavoro"},
            new String[]{"SCP",     "Sciopero"},
            new String[]{"REXCCAS", "Centro di Costo Rextart per Attività in Sede"},
            new String[]{"REXCCFO", "Centro di Costo Rextart per Formazione del Personale"}
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AbsenceTypeRepository absenceTypeRepository;

    @Value("${password.admin}")
    private String password;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        seedAdminUser();
        seedAbsenceTypes();
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

    private void seedAbsenceTypes() {
        int inserted = 0;
        for (String[] entry : ABSENCE_TYPES) {
            if (!absenceTypeRepository.existsByCode(entry[0])) {
                AbsenceType at = new AbsenceType();
                at.setCode(entry[0]);
                at.setDescription(entry[1]);
                absenceTypeRepository.save(at);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("DataInitializer: inserted {} absence types", inserted);
        }
    }
}