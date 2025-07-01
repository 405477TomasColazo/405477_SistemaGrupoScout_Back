package ar.edu.utn.frc.sistemascoutsjosehernandez.configs;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Arrays;

/**
 * DataLoader - Loads essential initial data into the database when the application starts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final RoleRepository roleRepository;
    private final StatusRepository statusRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final RolesXUserRepository rolesXUserRepository;
    private final CompetenceRepository competenceRepository;
    private final SuggestedActionRepository suggestedActionRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadInitialData() {
        log.info("Starting initial data loading process...");
        
        // Only load data if database is empty
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping initial data loading.");
            return;
        }

        try {
            loadBasicLookupTables();
            loadAdminUser();
            loadBasicCompetences();
            
            log.info("Initial data loading completed successfully!");
        } catch (Exception e) {
            log.error("Error during initial data loading: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load initial data", e);
        }
    }

    private void loadBasicLookupTables() {
        log.info("Loading basic lookup tables...");
        
        // Load Roles
        if (roleRepository.count() == 0) {
            Role familyRole = new Role();
            familyRole.setDescription("ROLE_FAMILY");
            
            Role educatorRole = new Role();
            educatorRole.setDescription("ROLE_EDUCATOR");
            
            Role adminRole = new Role();
            adminRole.setDescription("ROLE_ADMIN");
            
            List<Role> roles = Arrays.asList(familyRole, educatorRole, adminRole);
            roleRepository.saveAll(roles);
            log.info("Loaded {} roles", roles.size());
        }

        // Load Statuses
        if (statusRepository.count() == 0) {
            Status activeStatus = new Status();
            activeStatus.setDescription("ACTIVE");
            
            Status inactiveStatus = new Status();
            inactiveStatus.setDescription("INACTIVE");
            
            Status pendingStatus = new Status();
            pendingStatus.setDescription("PENDING");
            
            List<Status> statuses = Arrays.asList(activeStatus, inactiveStatus, pendingStatus);
            statusRepository.saveAll(statuses);
            log.info("Loaded {} statuses", statuses.size());
        }

        // Load Member Types
        if (memberTypeRepository.count() == 0) {
            MemberType protagonista = new MemberType();
            protagonista.setDescription("Protagonista");
            
            MemberType tutor = new MemberType();
            tutor.setDescription("Tutor");
            
            List<MemberType> memberTypes = Arrays.asList(protagonista, tutor);
            memberTypeRepository.saveAll(memberTypes);
            log.info("Loaded {} member types", memberTypes.size());
        }

        // Load Sections
        if (sectionRepository.count() == 0) {
            Section manada = new Section();
            manada.setDescription("Manada");
            manada.setMinAge(7);
            manada.setMaxAge(9);
            
            Section unidad = new Section();
            unidad.setDescription("Unidad");
            unidad.setMinAge(10);
            unidad.setMaxAge(13);
            
            Section caminantes = new Section();
            caminantes.setDescription("Caminantes");
            caminantes.setMinAge(14);
            caminantes.setMaxAge(17);
            
            Section rovers = new Section();
            rovers.setDescription("Rovers");
            rovers.setMinAge(18);
            rovers.setMaxAge(22);
            
            List<Section> sections = Arrays.asList(manada, unidad, caminantes, rovers);
            sectionRepository.saveAll(sections);
            log.info("Loaded {} sections", sections.size());
        }
    }

    private void loadAdminUser() {
        log.info("Creating default admin user...");
        
        // Get admin role
        Role adminRole = roleRepository.findAll().stream()
            .filter(role -> "ROLE_ADMIN".equals(role.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Admin role not found"));
        
        // Create admin user
        User adminUser = User.builder()
            .email("admin@scout.com")
            .lastName("Sistema")
            .passwordHash(passwordEncoder.encode("123"))
            .avatar("default")
            .build();
        
        User savedUser = userRepository.save(adminUser);
        
        // Assign admin role
        RolesXUser adminRoleAssignment = new RolesXUser();
        adminRoleAssignment.setUser(savedUser);
        adminRoleAssignment.setRole(adminRole);
        
        rolesXUserRepository.save(adminRoleAssignment);
        
        log.info("Created admin user with email: {}", adminUser.getEmail());
    }

    private void loadBasicCompetences() {
        log.info("Loading basic competences...");
        
        if (competenceRepository.count() == 0) {
            Competence comp1 = new Competence();
            comp1.setTitle("Desarrollo Personal");
            comp1.setDescription("Competencia enfocada en el crecimiento personal y valores éticos del scout");
            comp1.setGrowthArea(GrowthArea.PEACE_DEVELOPMENT);
            
            Competence comp2 = new Competence();
            comp2.setTitle("Cuidado de la Salud");
            comp2.setDescription("Competencia relacionada con el bienestar físico y mental");
            comp2.setGrowthArea(GrowthArea.HEALTH_WELLBEING);
            
            Competence comp3 = new Competence();
            comp3.setTitle("Cuidado del Ambiente");
            comp3.setDescription("Competencia sobre responsabilidad ambiental y sostenibilidad");
            comp3.setGrowthArea(GrowthArea.ENVIRONMENT);
            
            Competence comp4 = new Competence();
            comp4.setTitle("Habilidades para la Vida");
            comp4.setDescription("Competencia sobre destrezas prácticas y sociales");
            comp4.setGrowthArea(GrowthArea.LIFE_SKILLS);
            
            List<Competence> competences = Arrays.asList(comp1, comp2, comp3, comp4);
            competenceRepository.saveAll(competences);
            log.info("Loaded {} competences", competences.size());
            
            // Load suggested actions for each competence
            for (Competence competence : competences) {
                SuggestedAction action1 = new SuggestedAction();
                action1.setDescription("Acción sugerida 1 para " + competence.getTitle());
                action1.setCompetence(competence);
                
                SuggestedAction action2 = new SuggestedAction();
                action2.setDescription("Acción sugerida 2 para " + competence.getTitle());
                action2.setCompetence(competence);
                
                suggestedActionRepository.saveAll(Arrays.asList(action1, action2));
            }
            
            log.info("Loaded suggested actions for all competences");
        }
    }
}