package ar.edu.utn.frc.sistemascoutsjosehernandez.configs;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;

/**
 * DataLoaderTemplate - Template for loading initial data into the database
 * 
 * IMPORTANT: This is a TEMPLATE file with anonymized data for repository purposes.
 * For local development:
 * 1. Copy this file to DataLoader.java 
 * 2. Replace the anonymized data with real data as needed
 * 3. The real DataLoader.java is excluded from git via .gitignore
 * 
 * This template maintains the same structure and data relationships as the real DataLoader
 * but uses generic/anonymized information to protect sensitive personal data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoaderTemplate {

    private final RoleRepository roleRepository;
    private final StatusRepository statusRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final RolesXUserRepository rolesXUserRepository;
    private final CompetenceRepository competenceRepository;
    private final SuggestedActionRepository suggestedActionRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final MemberRepository memberRepository;
    private final EducatorRepository educatorRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AccountRepository accountRepository;
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
            loadPaymentMethods();
            loadChartOfAccounts();
            loadAdminUser();
            loadBasicCompetences();
            loadSampleFamilyData();
            loadSampleEducatorData();
            
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

            Status completedStatus = new Status();
            completedStatus.setDescription("COMPLETED");
            
            List<Status> statuses = Arrays.asList(activeStatus, inactiveStatus, pendingStatus, completedStatus);
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

    private void loadPaymentMethods() {
        log.info("Loading payment methods...");
        
        if (paymentMethodRepository.count() == 0) {
            PaymentMethod mercadoPago = new PaymentMethod();
            mercadoPago.setDescription("MERCADO_PAGO");
            
            PaymentMethod accountBalance = new PaymentMethod();
            accountBalance.setDescription("ACCOUNT_BALANCE");
            
            PaymentMethod cash = new PaymentMethod();
            cash.setDescription("CASH");
            
            PaymentMethod bankTransfer = new PaymentMethod();
            bankTransfer.setDescription("BANK_TRANSFER");
            
            List<PaymentMethod> paymentMethods = Arrays.asList(mercadoPago, accountBalance, cash, bankTransfer);
            paymentMethodRepository.saveAll(paymentMethods);
            log.info("Loaded {} payment methods", paymentMethods.size());
        } else {
            log.info("Payment methods already exist, skipping creation");
        }
    }

    private void loadChartOfAccounts() {
        log.info("Loading chart of accounts...");
        
        if (accountRepository.count() == 0) {
            // ASSET ACCOUNTS (Activos)
            Account cash = Account.builder()
                    .code("1.1.1")
                    .name("Caja")
                    .type(AccountType.ASSET)
                    .active(true)
                    .description("Dinero en efectivo")
                    .build();

            Account bank = Account.builder()
                    .code("1.1.2")
                    .name("Banco")
                    .type(AccountType.ASSET)
                    .active(true)
                    .description("Cuentas bancarias y transferencias")
                    .build();

            Account accountsReceivable = Account.builder()
                    .code("1.2.1")
                    .name("Cuentas por cobrar")
                    .type(AccountType.ASSET)
                    .active(true)
                    .description("Cuotas y pagos de miembros pendientes")
                    .build();

            // LIABILITY ACCOUNTS (Pasivos)
            Account accountsPayable = Account.builder()
                    .code("2.1.1")
                    .name("Cuentas a Pagar")
                    .type(AccountType.LIABILITY)
                    .active(true)
                    .description("Pagos pendientes a proveedores")
                    .build();

            // EQUITY ACCOUNTS (Patrimonio)
            Account equity = Account.builder()
                    .code("3.1.1")
                    .name("Patrimonio neto")
                    .type(AccountType.EQUITY)
                    .active(true)
                    .description("Patrimonio neto y fondos acumulados del grupo Scout")
                    .build();

            // INCOME ACCOUNTS (Ingresos)
            Account monthlyFeesIncome = Account.builder()
                    .code("4.1.1")
                    .name("Ingresos por cuotas mensuales")
                    .type(AccountType.INCOME)
                    .active(true)
                    .description("Ingresos por cuotas mensuales de scouts")
                    .build();

            Account eventsIncome = Account.builder()
                    .code("4.1.2")
                    .name("Ingresos por eventos")
                    .type(AccountType.INCOME)
                    .active(true)
                    .description("Ingresos por eventos y actividades scouts")
                    .build();

            Account donationsIncome = Account.builder()
                    .code("4.2.1")
                    .name("Ingresos por donaciones")
                    .type(AccountType.INCOME)
                    .active(true)
                    .description("Donaciones y subvenciones recibidas")
                    .build();

            Account otherIncome = Account.builder()
                    .code("4.9.1")
                    .name("Otros ingresos")
                    .type(AccountType.INCOME)
                    .active(true)
                    .description("Ingresos y rentas varios")
                    .build();

            // EXPENSE ACCOUNTS (Egresos)
            Account servicesExpense = Account.builder()
                    .code("5.1.1")
                    .name("Gastos de servicios")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Servicios públicos, internet, teléfono y otros servicios")
                    .build();

            Account materialsExpense = Account.builder()
                    .code("5.1.2")
                    .name("Gastos de materiales")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Materiales, uniformes y suministros para los scouts")
                    .build();

            Account eventsExpense = Account.builder()
                    .code("5.1.3")
                    .name("Gastos de eventos")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Costos relacionados con eventos y actividades scouts")
                    .build();

            Account administrativeExpense = Account.builder()
                    .code("5.2.1")
                    .name("Gastos administrativos")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Costos administrativos y gastos de oficina")
                    .build();

            Account facilitiesExpense = Account.builder()
                    .code("5.2.2")
                    .name("Gastos de instalaciones")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Costos de alquiler, mantenimiento e instalaciones")
                    .build();

            Account transportationExpense = Account.builder()
                    .code("5.3.1")
                    .name("Gastos de transporte")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Costos de transporte para eventos y actividades")
                    .build();

            Account trainingExpense = Account.builder()
                    .code("5.3.2")
                    .name("Gastos de formación")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Costos de capacitación y desarrollo de educadores")
                    .build();

            Account otherExpense = Account.builder()
                    .code("5.9.1")
                    .name("Otros gastos")
                    .type(AccountType.EXPENSE)
                    .active(true)
                    .description("Gastos y costes varios")
                    .build();

            // Save all accounts
            List<Account> accounts = Arrays.asList(
                    cash, bank, accountsReceivable, accountsPayable, equity,
                    monthlyFeesIncome, eventsIncome, donationsIncome, otherIncome,
                    servicesExpense, materialsExpense, eventsExpense, administrativeExpense,
                    facilitiesExpense, transportationExpense, trainingExpense, otherExpense
            );

            accountRepository.saveAll(accounts);
            log.info("Loaded {} accounting accounts", accounts.size());
        } else {
            log.info("Chart of accounts already exists, skipping creation");
        }
    }

    private void loadAdminUser() {
        log.info("Creating default admin user...");
        
        // Get admin role and required types
        Role adminRole = roleRepository.findAll().stream()
            .filter(role -> "ROLE_ADMIN".equals(role.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
        Status activeStatus = statusRepository.findAll().stream()
            .filter(status -> "ACTIVE".equals(status.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Active status not found"));
            
        MemberType tutorType = memberTypeRepository.findAll().stream()
            .filter(type -> "Tutor".equals(type.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tutor member type not found"));
        
        // Create admin user
        User adminUser = User.builder()
            .email("admin@scout.local")
            .lastName("Administrador")
            .passwordHash(passwordEncoder.encode("admin123"))
            .avatar("default")
            .build();
        
        User savedUser = userRepository.save(adminUser);
        
        // Assign admin role
        RolesXUser adminRoleAssignment = new RolesXUser();
        adminRoleAssignment.setUser(savedUser);
        adminRoleAssignment.setRole(adminRole);
        rolesXUserRepository.save(adminRoleAssignment);
        
        // Create admin family group
        FamilyGroup adminFamily = FamilyGroup.builder()
            .name("Administración")
            .user(savedUser)
            .build();
        FamilyGroup savedFamily = familyGroupRepository.save(adminFamily);
        
        // Create admin member
        Member adminMember = Member.builder()
            .user(savedUser)
            .name("Administrador")
            .lastname("Sistema")
            .dni("11111111")
            .birthdate(LocalDate.of(1980, 1, 1))
            .address("Dirección del Sistema")
            .contactPhone("1111111111")
            .email("admin@scout.local")
            .isTutor(true)
            .memberType(tutorType)
            .status(activeStatus)
            .familyGroup(savedFamily)
            .accountBalance(BigDecimal.ZERO)
            .isActive(true)
            .build();
        
        Member savedMember = memberRepository.save(adminMember);
        
        // Set admin as main contact
        savedFamily.setMainContact(savedMember);
        familyGroupRepository.save(savedFamily);
        
        log.info("Created admin user with email: {} and family group: {}", adminUser.getEmail(), savedFamily.getName());
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

    private void loadSampleFamilyData() {
        log.info("Loading sample family data...");
        
        // Check if family data already exists
        if (familyGroupRepository.findAll().stream()
                .anyMatch(fg -> "Familia Ejemplo 1".equals(fg.getName()))) {
            log.info("Sample family data already exists. Skipping family data loading.");
            return;
        }

        // Get required lookup entities
        Role familyRole = roleRepository.findAll().stream()
            .filter(role -> "ROLE_FAMILY".equals(role.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Family role not found"));
        
        Status activeStatus = statusRepository.findAll().stream()
            .filter(status -> "ACTIVE".equals(status.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Active status not found"));
            
        MemberType protagonistType = memberTypeRepository.findAll().stream()
            .filter(type -> "Protagonista".equals(type.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Protagonista member type not found"));
            
        MemberType tutorType = memberTypeRepository.findAll().stream()
            .filter(type -> "Tutor".equals(type.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tutor member type not found"));

        // Sections
        Section manada = sectionRepository.findAll().stream()
            .filter(s -> "Manada".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Manada section not found"));
            
        Section unidad = sectionRepository.findAll().stream()
            .filter(s -> "Unidad".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Unidad section not found"));
            
        Section caminantes = sectionRepository.findAll().stream()
            .filter(s -> "Caminantes".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Caminantes section not found"));
            
        Section rovers = sectionRepository.findAll().stream()
            .filter(s -> "Rovers".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Rovers section not found"));

        // Create sample families with generic data
        
        // Family 1
        User userFamily1 = createFamilyUser("familia1@scout.local", "Familia1", familyRole);
        FamilyGroup family1 = createFamilyGroup("Familia Ejemplo 1", userFamily1);
        Member tutorFamily1 = createTutor("María", "González", "1111111111", "maria.gonzalez@familia.local", 
            "Dirección Ejemplo 123", LocalDate.of(1980, 5, 15), "22222222", tutorType, activeStatus, family1);
        family1.setMainContact(tutorFamily1);
        familyGroupRepository.save(family1);
        
        createMember("Juan", "González", "33333333", LocalDate.of(2010, 9, 23), unidad, protagonistType, activeStatus, family1);
        createMember("Ana", "González", "44444444", LocalDate.of(2013, 4, 17), manada, protagonistType, activeStatus, family1);

        // Family 2
        User userFamily2 = createFamilyUser("familia2@scout.local", "Familia2", familyRole);
        FamilyGroup family2 = createFamilyGroup("Familia Ejemplo 2", userFamily2);
        Member tutorFamily2 = createTutor("Carlos", "Rodríguez", "2222222222", "carlos.rodriguez@familia.local", 
            "Avenida Principal 456", LocalDate.of(1978, 8, 20), "55555555", tutorType, activeStatus, family2);
        family2.setMainContact(tutorFamily2);
        familyGroupRepository.save(family2);
        
        createMember("Luis", "Rodríguez", "66666666", LocalDate.of(2005, 12, 27), rovers, protagonistType, activeStatus, family2);
        createMember("Carmen", "Rodríguez", "77777777", LocalDate.of(2008, 6, 20), caminantes, protagonistType, activeStatus, family2);

        // Family 3
        User userFamily3 = createFamilyUser("familia3@scout.local", "Familia3", familyRole);
        FamilyGroup family3 = createFamilyGroup("Familia Ejemplo 3", userFamily3);
        Member tutorFamily3 = createTutor("Elena", "Martínez", "3333333333", "elena.martinez@familia.local", 
            "Calle Secundaria 789", LocalDate.of(1975, 12, 10), "88888888", tutorType, activeStatus, family3);
        family3.setMainContact(tutorFamily3);
        familyGroupRepository.save(family3);
        
        createMember("Pedro", "Martínez", "99999999", LocalDate.of(2007, 5, 30), caminantes, protagonistType, activeStatus, family3);
        createMember("Sofía", "Martínez", "10101010", LocalDate.of(2012, 11, 26), unidad, protagonistType, activeStatus, family3);

        log.info("Loaded sample family data with {} families", 3);
    }

    private void loadSampleEducatorData() {
        log.info("Loading sample educator data...");
        
        if (educatorRepository.count() > 0) {
            log.info("Sample educator data already exists. Skipping educator data loading.");
            return;
        }

        // Get required lookup entities
        Role educatorRole = roleRepository.findAll().stream()
            .filter(role -> "ROLE_EDUCATOR".equals(role.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Educator role not found"));
        
        Status activeStatus = statusRepository.findAll().stream()
            .filter(status -> "ACTIVE".equals(status.getDescription()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Active status not found"));

        // Sections
        Section manada = sectionRepository.findAll().stream()
            .filter(s -> "Manada".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Manada section not found"));
            
        Section unidad = sectionRepository.findAll().stream()
            .filter(s -> "Unidad".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Unidad section not found"));
            
        Section caminantes = sectionRepository.findAll().stream()
            .filter(s -> "Caminantes".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Caminantes section not found"));
            
        Section rovers = sectionRepository.findAll().stream()
            .filter(s -> "Rovers".equals(s.getDescription()))
            .findFirst().orElseThrow(() -> new RuntimeException("Rovers section not found"));

        // Create sample educators with generic data
        createEducator("Roberto", "Silva", "12121212", 
            LocalDate.of(1985, 3, 24), "roberto.silva@educador.local", 
            "Dirección Educador 100", "Jefe de Manada", manada, educatorRole, activeStatus);

        createEducator("Laura", "Fernández", "13131313", 
            LocalDate.of(1990, 12, 21), "laura.fernandez@educador.local", 
            "Avenida Educadores 200", "Jefe de Unidad Scout", unidad, educatorRole, activeStatus);

        createEducator("Miguel", "López", "14141414", 
            LocalDate.of(1982, 12, 4), "miguel.lopez@educador.local", 
            "Calle Educadores 300", "Jefe de Comunidad Caminante", caminantes, educatorRole, activeStatus);

        createEducator("Patricia", "García", "15151515", 
            LocalDate.of(1975, 1, 14), "patricia.garcia@educador.local", 
            "Plaza Educadores 400", "Jefe de Comunidad Rover", rovers, educatorRole, activeStatus);

        log.info("Loaded sample educator data with {} educators", 4);
    }

    private User createFamilyUser(String email, String lastName, Role familyRole) {
        User user = User.builder()
            .email(email)
            .lastName(lastName)
            .passwordHash(passwordEncoder.encode("familia123"))
            .avatar("default")
            .build();
        
        User savedUser = userRepository.save(user);
        
        RolesXUser roleAssignment = new RolesXUser();
        roleAssignment.setUser(savedUser);
        roleAssignment.setRole(familyRole);
        rolesXUserRepository.save(roleAssignment);
        
        return savedUser;
    }

    private FamilyGroup createFamilyGroup(String name, User user) {
        FamilyGroup familyGroup = FamilyGroup.builder()
            .name(name)
            .user(user)
            .build();
        
        return familyGroupRepository.save(familyGroup);
    }

    private Member createTutor(String name, String lastName, String phone, String email, 
                              String address, LocalDate birthdate, String dni,
                              MemberType tutorType, Status activeStatus, FamilyGroup familyGroup) {
        Member tutor = Member.builder()
            .user(familyGroup.getUser())
            .name(name)
            .lastname(lastName)
            .contactPhone(phone)
            .email(email)
            .address(address)
            .birthdate(birthdate)
            .dni(dni)
            .isTutor(true)
            .memberType(tutorType)
            .status(activeStatus)
            .familyGroup(familyGroup)
            .accountBalance(BigDecimal.ZERO)
            .isActive(true)
            .build();
        
        return memberRepository.save(tutor);
    }

    private Member createMember(String name, String lastName, String dni, LocalDate birthdate,
                               Section section, MemberType protagonistType, Status activeStatus, 
                               FamilyGroup familyGroup) {
        Member member = Member.builder()
            .user(familyGroup.getUser())
            .name(name)
            .lastname(lastName)
            .dni(dni)
            .birthdate(birthdate)
            .address(familyGroup.getMainContact().getAddress())
            .section(section)
            .isTutor(false)
            .memberType(protagonistType)
            .status(activeStatus)
            .familyGroup(familyGroup)
            .accountBalance(BigDecimal.ZERO)
            .isActive(true)
            .build();
        
        return memberRepository.save(member);
    }

    private void createEducator(String name, String lastName, String dni, LocalDate birthdate,
                               String email, String address, String position, Section section,
                               Role educatorRole, Status activeStatus) {
        // Create user for educator
        User educatorUser = User.builder()
            .email(email)
            .lastName(lastName)
            .passwordHash(passwordEncoder.encode("educador123"))
            .avatar("default")
            .build();
        
        User savedUser = userRepository.save(educatorUser);
        
        RolesXUser roleAssignment = new RolesXUser();
        roleAssignment.setUser(savedUser);
        roleAssignment.setRole(educatorRole);
        rolesXUserRepository.save(roleAssignment);
        
        // Create family group for educator
        FamilyGroup educatorFamily = FamilyGroup.builder()
            .name("Familia " + lastName)
            .user(savedUser)
            .build();
        familyGroupRepository.save(educatorFamily);
        
        // Create educator
        Educator educator = Educator.builder()
            .user(savedUser)
            .name(name)
            .lastname(lastName)
            .birthdate(birthdate)
            .dni(dni)
            .position(position)
            .section(section)
            .address(address)
            .startDate(LocalDate.of(2020, 1, 1))
            .accountBalance(BigDecimal.ZERO)
            .status(activeStatus)
            .build();
        
        educatorRepository.save(educator);
    }
}