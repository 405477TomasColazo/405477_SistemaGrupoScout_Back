package ar.edu.utn.frc.sistemascoutsjosehernandez.configs;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsCategory;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsArticleRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsCategoryRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CompetenceRepository competenceRepository;
    private final RoleRepository roleRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final StatusRepository statusRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final MemberRepository memberRepository;
    private final RolesXUserRepository rolesXUserRepository;
    private final EducatorRepository educatorRepository;
    private final FeeRepository feeRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final MarchSheetRepository marchSheetRepository;
    private final CompetenceProgressRepository competenceProgressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        
//        // 1. Datos fundamentales del sistema
//        if (roleRepository.count() == 0) {
//            log.info("Cargando roles del sistema...");
//            initializeRoles();
//            log.info("Roles del sistema cargados exitosamente.");
//        }
//
//        if (sectionRepository.count() == 0) {
//            log.info("Cargando secciones scout...");
//            initializeSections();
//            log.info("Secciones scout cargadas exitosamente.");
//        }
//
//        if (memberTypeRepository.count() == 0) {
//            log.info("Cargando tipos de miembro...");
//            initializeMemberTypes();
//            log.info("Tipos de miembro cargados exitosamente.");
//        }
//
//        if (statusRepository.count() == 0) {
//            log.info("Cargando estados...");
//            initializeStatuses();
//            log.info("Estados cargados exitosamente.");
//        }
//
//        // 2. Usuarios y estructura familiar
//        if (userRepository.count() == 0) {
//            log.info("Cargando usuarios del sistema...");
//            initializeUsers();
//            log.info("Usuarios del sistema cargados exitosamente.");
//        }
//
//        if (familyGroupRepository.count() == 0) {
//            log.info("Cargando grupos familiares...");
//            initializeFamilyGroups();
//            log.info("Grupos familiares cargados exitosamente.");
//        }
//
//        if (educatorRepository.count() == 0) {
//            log.info("Asignando educadores a secciones...");
//            initializeEducators();
//            log.info("Educadores asignados exitosamente.");
//        }
//
//        // 3. Sistema financiero
//        if (feeRepository.count() == 0) {
//            log.info("Cargando cuotas mensuales...");
//            initializeFees();
//            log.info("Cuotas mensuales cargadas exitosamente.");
//        }
//
//        // 4. Eventos
//        if (eventRepository.count() == 0) {
//            log.info("Cargando eventos scout...");
//            initializeEvents();
//            log.info("Eventos scout cargados exitosamente.");
//        }
//
//        // 5. Sistema de progresión
//        if (competenceRepository.count() == 0) {
//            log.info("Cargando competencias de ejemplo...");
//            initializeCompetences();
//            log.info("Competencias de ejemplo cargadas exitosamente.");
//        }
//
//        if (marchSheetRepository.count() == 0) {
//            log.info("Cargando hojas de marcha...");
//            initializeMarchSheets();
//            log.info("Hojas de marcha cargadas exitosamente.");
//        }
//
//        // 6. Sistema de noticias
//        if (newsCategoryRepository.count() == 0) {
//            log.info("Cargando categorías de noticias de ejemplo...");
//            initializeNewsCategories();
//            log.info("Categorías de noticias cargadas exitosamente.");
//        }
//
//        if (newsArticleRepository.count() == 0) {
//            log.info("Cargando noticias de ejemplo...");
//            initializeNewsArticles();
//            log.info("Noticias de ejemplo cargadas exitosamente.");
//        }
//    }
//
//    private void initializeCompetences() {
//        List<Competence> competences = Arrays.asList(
//            // SALUD Y BIENESTAR
//            createCompetence(
//                "Adopto y promuevo hábitos de alimentación e higiene saludables",
//                "Desarrollo hábitos saludables de alimentación e higiene personal que contribuyen a mi bienestar físico y mental.",
//                GrowthArea.HEALTH_WELLBEING,
//                Arrays.asList(
//                    "Mantengo el ambiente limpio y ordenado, en mi hogar, en mi espacio de reunión con el equipo",
//                    "Sostengo una alimentación equilibrada que incluya variedad de alimentos con recetas saludables",
//                    "Practico hábitos de higiene personal diarios"
//                ),
//                Arrays.asList(
//                    "¿Por qué debe importarme lo que como?",
//                    "¿Cuál es la importancia de aprender a cocinar?",
//                    "¿Cómo impactan mis hábitos de higiene en mi relación con otros?"
//                )
//            ),
//            createCompetence(
//                "Cuido mi cuerpo desarrollando destrezas físicas",
//                "Mantengo una vida activa desarrollando habilidades físicas y deportivas que fortalezcan mi cuerpo.",
//                GrowthArea.HEALTH_WELLBEING,
//                Arrays.asList(
//                    "Practico regularmente algún deporte o actividad física",
//                    "Participo en juegos y actividades que desarrollen mi coordinación",
//                    "Aprendo técnicas de relajación y manejo del estrés"
//                ),
//                Arrays.asList(
//                    "¿Qué beneficios tiene para mí mantenerme activo físicamente?",
//                    "¿Cómo puedo equilibrar el tiempo de estudio con la actividad física?"
//                )
//            ),
//            createCompetence(
//                "Reconozco mis emociones y las expreso de manera positiva",
//                "Desarrollo inteligencia emocional para reconocer, comprender y expresar mis sentimientos de manera constructiva.",
//                GrowthArea.HEALTH_WELLBEING,
//                Arrays.asList(
//                    "Identifico y nombro mis emociones cuando las experimento",
//                    "Busco formas saludables de expresar lo que siento",
//                    "Pido ayuda cuando me siento abrumado emocionalmente"
//                ),
//                Arrays.asList(
//                    "¿Qué emociones me resultan más difíciles de manejar?",
//                    "¿Cómo puedo comunicar mejor lo que siento a mi familia y amigos?"
//                )
//            ),
//
//            // PAZ Y DESARROLLO
//            createCompetence(
//                "Contribuyo al desarrollo de mi comunidad",
//                "Participo activamente en proyectos y actividades que beneficien a mi comunidad y promuevan el bien común.",
//                GrowthArea.PEACE_DEVELOPMENT,
//                Arrays.asList(
//                    "Participo en proyectos de servicio comunitario",
//                    "Colaboro con organizaciones locales en actividades solidarias",
//                    "Promuevo valores de respeto y convivencia en mi entorno"
//                ),
//                Arrays.asList(
//                    "¿Qué problemas veo en mi comunidad que podría ayudar a resolver?",
//                    "¿Cómo puedo involucrar a otros jóvenes en actividades de servicio?"
//                )
//            ),
//            createCompetence(
//                "Promuevo la paz y resuelvo conflictos de manera constructiva",
//                "Desarrollo habilidades para mediar conflictos y promover la convivencia pacífica en mis relaciones.",
//                GrowthArea.PEACE_DEVELOPMENT,
//                Arrays.asList(
//                    "Aprendo técnicas de mediación y resolución de conflictos",
//                    "Practico la escucha activa cuando hay desacuerdos",
//                    "Busco soluciones que beneficien a todas las partes involucradas"
//                ),
//                Arrays.asList(
//                    "¿Qué estrategias uso actualmente para resolver conflictos?",
//                    "¿Cómo puedo ser un agente de paz en mi familia y grupo de amigos?"
//                )
//            ),
//            createCompetence(
//                "Valoro y respeto la diversidad cultural",
//                "Reconozco y celebro las diferencias culturales, promoviendo la inclusión y el respeto mutuo.",
//                GrowthArea.PEACE_DEVELOPMENT,
//                Arrays.asList(
//                    "Aprendo sobre diferentes culturas y tradiciones",
//                    "Participo en eventos multiculturales",
//                    "Defiendo los derechos de personas de diferentes orígenes"
//                ),
//                Arrays.asList(
//                    "¿Qué puedo aprender de culturas diferentes a la mía?",
//                    "¿Cómo puedo combatir los prejuicios y estereotipos?"
//                )
//            ),
//
//            // AMBIENTE
//            createCompetence(
//                "Cuido y protejo el medio ambiente",
//                "Desarrollo conciencia ambiental y prácticas sostenibles que contribuyan a la protección del planeta.",
//                GrowthArea.ENVIRONMENT,
//                Arrays.asList(
//                    "Implemento prácticas de reciclaje en mi hogar",
//                    "Reduzco mi consumo de recursos naturales",
//                    "Participo en actividades de conservación ambiental"
//                ),
//                Arrays.asList(
//                    "¿Qué acciones concretas puedo tomar para reducir mi impacto ambiental?",
//                    "¿Cómo puedo involucrar a mi familia en prácticas más sostenibles?"
//                )
//            ),
//            createCompetence(
//                "Promuevo el uso responsable de recursos naturales",
//                "Utilizo de manera consciente y responsable los recursos naturales, promoviendo su conservación.",
//                GrowthArea.ENVIRONMENT,
//                Arrays.asList(
//                    "Ahorro agua y energía en mis actividades diarias",
//                    "Elijo productos eco-amigables cuando es posible",
//                    "Educo a otros sobre la importancia del uso responsable de recursos"
//                ),
//                Arrays.asList(
//                    "¿Cuántos recursos naturales consumo diariamente?",
//                    "¿Qué alternativas sostenibles puedo implementar en mi vida?"
//                )
//            ),
//            createCompetence(
//                "Participo en la protección de espacios naturales",
//                "Contribuyo activamente a la conservación y protección de espacios naturales en mi entorno.",
//                GrowthArea.ENVIRONMENT,
//                Arrays.asList(
//                    "Participo en jornadas de limpieza de espacios naturales",
//                    "Aprendo sobre la flora y fauna local",
//                    "Promuevo el respeto por los espacios verdes urbanos"
//                ),
//                Arrays.asList(
//                    "¿Qué espacios naturales cerca de mi hogar necesitan protección?",
//                    "¿Cómo puedo contribuir a crear más áreas verdes en mi comunidad?"
//                )
//            ),
//
//            // HABILIDADES PARA LA VIDA
//            createCompetence(
//                "Desarrollo habilidades de liderazgo",
//                "Aprendo a liderar equipos y proyectos, inspirando a otros a trabajar hacia objetivos comunes.",
//                GrowthArea.LIFE_SKILLS,
//                Arrays.asList(
//                    "Lidero un proyecto en mi equipo scout",
//                    "Organizo actividades para mi grupo de amigos",
//                    "Tomo iniciativa para resolver problemas grupales"
//                ),
//                Arrays.asList(
//                    "¿Qué cualidades tiene un buen líder?",
//                    "¿Cómo puedo inspirar a otros a participar en proyectos positivos?"
//                )
//            ),
//            createCompetence(
//                "Gestiono mi tiempo y organizo mis actividades",
//                "Desarrollo habilidades de planificación y organización para equilibrar mis responsabilidades y actividades.",
//                GrowthArea.LIFE_SKILLS,
//                Arrays.asList(
//                    "Uso una agenda para planificar mis actividades",
//                    "Establecer prioridades en mis tareas diarias",
//                    "Equilibro tiempo de estudio, descanso y recreación"
//                ),
//                Arrays.asList(
//                    "¿Cómo puedo aprovechar mejor mi tiempo?",
//                    "¿Qué actividades son más importantes para mi desarrollo personal?"
//                )
//            ),
//            createCompetence(
//                "Desarrollo habilidades de comunicación efectiva",
//                "Aprendo a comunicarme de manera clara, asertiva y empática en diferentes contextos.",
//                GrowthArea.LIFE_SKILLS,
//                Arrays.asList(
//                    "Practico hablar en público con confianza",
//                    "Aprendo a escuchar activamente a otros",
//                    "Expreso mis ideas de manera clara y respetuosa"
//                ),
//                Arrays.asList(
//                    "¿Qué barreras encuentro para comunicarme efectivamente?",
//                    "¿Cómo puedo mejorar mi capacidad de escuchar a otros?"
//                )
//            ),
//            createCompetence(
//                "Tomo decisiones responsables e informadas",
//                "Desarrollo la capacidad de tomar decisiones reflexivas considerando sus consecuencias e impacto.",
//                GrowthArea.LIFE_SKILLS,
//                Arrays.asList(
//                    "Analizo las opciones antes de tomar decisiones importantes",
//                    "Considero las consecuencias de mis acciones",
//                    "Busco información y consejos cuando necesito decidir"
//                ),
//                Arrays.asList(
//                    "¿Qué proceso sigo para tomar decisiones importantes?",
//                    "¿Cómo puedo aprender de las decisiones que no salieron como esperaba?"
//                )
//            )
//        );
//
//        competenceRepository.saveAll(competences);
//    }
//
//    private Competence createCompetence(String title, String description, GrowthArea growthArea,
//                                      List<String> suggestedActionTexts, List<String> guidingQuestions) {
//        Competence competence = Competence.builder()
//                .title(title)
//                .description(description)
//                .growthArea(growthArea)
//                .guidingQuestions(guidingQuestions)
//                .build();
//
//        // Crear acciones sugeridas
//        List<SuggestedAction> suggestedActions = suggestedActionTexts.stream()
//                .map(text -> SuggestedAction.builder()
//                        .description(text)
//                        .competence(competence)
//                        .build())
//                .toList();
//
//        competence.setSuggestedActions(suggestedActions);
//        return competence;
//    }
//
//    private void initializeNewsCategories() {
//        List<NewsCategory> categories = Arrays.asList(
//            NewsCategory.builder()
//                .name("Actividades")
//                .slug("actividades")
//                .description("Noticias sobre campamentos, excursiones y actividades del grupo")
//                .color("#10B981")
//                .build(),
//            NewsCategory.builder()
//                .name("Eventos")
//                .slug("eventos")
//                .description("Información sobre eventos especiales y celebraciones")
//                .color("#3B82F6")
//                .build(),
//            NewsCategory.builder()
//                .name("Comunidad")
//                .slug("comunidad")
//                .description("Proyectos comunitarios y actividades de servicio")
//                .color("#8B5CF6")
//                .build(),
//            NewsCategory.builder()
//                .name("Formación")
//                .slug("formacion")
//                .description("Talleres, cursos y actividades de formación scout")
//                .color("#F59E0B")
//                .build()
//        );
//
//        newsCategoryRepository.saveAll(categories);
//    }
//
//    private void initializeNewsArticles() {
//        // Buscar el usuario con ID 4
//        Optional<User> authorOpt = userRepository.findById(4);
//        if (authorOpt.isEmpty()) {
//            log.warn("No se encontró usuario con ID 4, no se crearán noticias de ejemplo");
//            return;
//        }
//
//        User author = authorOpt.get();
//
//        // Obtener categorías
//        NewsCategory actividadesCategory = newsCategoryRepository.findBySlug("actividades").orElse(null);
//        NewsCategory eventosCategory = newsCategoryRepository.findBySlug("eventos").orElse(null);
//        NewsCategory comunidadCategory = newsCategoryRepository.findBySlug("comunidad").orElse(null);
//
//        List<NewsArticle> articles = Arrays.asList(
//            NewsArticle.builder()
//                .title("Gran Campamento de Verano 2025")
//                .slug("gran-campamento-verano-2025")
//                .summary("¡Abierta la inscripción para nuestro campamento anual! Este año nos dirigimos a las sierras de Córdoba para una aventura inolvidable.")
//                .content("<p>Estimadas familias scout,</p>" +
//                         "<p>Nos complace anunciar que ya está abierta la inscripción para nuestro <strong>Gran Campamento de Verano 2025</strong>. Este año hemos elegido un destino especial en las hermosas sierras de Córdoba, donde viviremos una experiencia única llena de aventuras, aprendizaje y hermandad scout.</p>" +
//                         "<h2>Detalles del Campamento</h2>" +
//                         "<ul>" +
//                         "<li><strong>Fechas:</strong> Del 15 al 22 de enero de 2025</li>" +
//                         "<li><strong>Lugar:</strong> Campamento 'Los Pinos', Villa General Belgrano</li>" +
//                         "<li><strong>Edades:</strong> Todas las secciones (7 a 21 años)</li>" +
//                         "<li><strong>Costo:</strong> $45.000 por participante</li>" +
//                         "</ul>" +
//                         "<h2>Actividades Programadas</h2>" +
//                         "<p>Durante estos 7 días intensivos, los scouts participarán en:</p>" +
//                         "<ul>" +
//                         "<li>Talleres de nudos y pionerismo</li>" +
//                         "<li>Caminatas por senderos de montaña</li>" +
//                         "<li>Fogones nocturnos con canciones tradicionales</li>" +
//                         "<li>Juegos de orientación y rastreo</li>" +
//                         "<li>Talleres de cocina al aire libre</li>" +
//                         "<li>Actividades acuáticas en el río</li>" +
//                         "</ul>" +
//                         "<p>Para inscribirse, completar el formulario disponible en nuestra sede o contactar a los dirigentes de cada sección.</p>" +
//                         "<p><em>¡Los cupos son limitados! No te quedes sin tu lugar en esta gran aventura.</em></p>")
//                .featuredImage("/media/caminante.JPG")
//                .author(author)
//                .status(NewsArticle.NewsStatus.PUBLISHED)
//                .publishDate(LocalDateTime.now().minusDays(2))
//                .createdAt(LocalDateTime.now().minusDays(3))
//                .updatedAt(LocalDateTime.now().minusDays(2))
//                .viewsCount(127)
//                .categories(actividadesCategory != null ? Set.of(actividadesCategory) : Set.of())
//                .build(),
//
//            NewsArticle.builder()
//                .title("Proyecto Comunitario: Restauración del Parque San Martín")
//                .slug("proyecto-comunitario-restauracion-parque-san-martin")
//                .summary("Los rovers han completado con éxito el proyecto de restauración del parque local, beneficiando a toda la comunidad.")
//                .content("<p>Con gran orgullo compartimos el éxito de nuestro último proyecto comunitario llevado a cabo por la sección Rovers de nuestro grupo scout.</p>" +
//                         "<h2>El Proyecto</h2>" +
//                         "<p>Durante los últimos tres meses, nuestros rovers trabajaron incansablemente en la <strong>restauración del Parque San Martín</strong>, un espacio verde fundamental para nuestro barrio que había sido descuidado en los últimos años.</p>" +
//                         "<h2>Actividades Realizadas</h2>" +
//                         "<p>El proyecto incluyó las siguientes actividades:</p>" +
//                         "<ul>" +
//                         "<li>Limpieza profunda de 2 hectáreas de parque</li>" +
//                         "<li>Plantación de 50 árboles nativos</li>" +
//                         "<li>Instalación de 8 cestos de basura</li>" +
//                         "<li>Reparación de bancos y juegos infantiles</li>" +
//                         "<li>Creación de senderos peatonales</li>" +
//                         "<li>Instalación de cartelería educativa sobre cuidado ambiental</li>" +
//                         "</ul>" +
//                         "<h2>Impacto en la Comunidad</h2>" +
//                         "<p>Gracias a este esfuerzo, el parque ahora recibe diariamente a más de 200 familias que disfrutan de un espacio renovado y seguro. El proyecto ha sido reconocido por la municipalidad local como un ejemplo de compromiso juvenil con el ambiente.</p>" +
//                         "<blockquote>" +
//                         "<p>'Este tipo de iniciativas demuestran el verdadero espíritu scout: servir a la comunidad sin esperar nada a cambio. Estamos muy orgullosos de nuestros rovers.'</p>" +
//                         "<cite>- Jefe de Grupo</cite>" +
//                         "</blockquote>" +
//                         "<p>¡Felicitamos a todos los rovers participantes por su dedicación y compromiso con nuestra comunidad!</p>")
//                .featuredImage("/media/rover.JPG")
//                .author(author)
//                .status(NewsArticle.NewsStatus.PUBLISHED)
//                .publishDate(LocalDateTime.now().minusDays(5))
//                .createdAt(LocalDateTime.now().minusDays(6))
//                .updatedAt(LocalDateTime.now().minusDays(5))
//                .viewsCount(89)
//                .categories(comunidadCategory != null ? Set.of(comunidadCategory) : Set.of())
//                .build(),
//
//            NewsArticle.builder()
//                .title("Celebración del Día del Scout: Una Tradición que Continúa")
//                .slug("celebracion-dia-scout-tradicion-continua")
//                .summary("Revivimos los momentos más emotivos de nuestra celebración anual del Día del Scout, donde renovamos nuestras promesas y celebramos la hermandad.")
//                .content("<p>El pasado sábado 23 de febrero celebramos con gran alegría el <strong>Día del Scout</strong>, una fecha muy especial para toda nuestra comunidad scout donde renovamos nuestro compromiso con los valores y principios del escultismo.</p>" +
//                         "<h2>Una Jornada Memorable</h2>" +
//                         "<p>La celebración comenzó temprano en la mañana con la tradicional <em>izada de banderas</em>, seguida por actividades especiales para cada sección:</p>" +
//                         "<h3>Manada</h3>" +
//                         "<p>Los lobatos y lobeznas participaron en el 'Gran Juego de la Selva', donde Mowgli y sus amigos los guiaron por diversas pruebas que pusieron a prueba su astucia y trabajo en equipo.</p>" +
//                         "<h3>Scouts</h3>" +
//                         "<p>Las patrullas compitieron en un emocionante torneo de habilidades scout que incluyó:</p>" +
//                         "<ul>" +
//                         "<li>Competencia de nudos cronometrada</li>" +
//                         "<li>Construcción de torres con materiales naturales</li>" +
//                         "<li>Juegos de kim y observación</li>" +
//                         "<li>Carrera de relevos con obstáculos</li>" +
//                         "</ul>" +
//                         "<h3>Caminantes</h3>" +
//                         "<p>Los caminantes organizaron un taller de primeros auxilios para toda la comunidad del barrio, demostrando su compromiso con el servicio comunitario.</p>" +
//                         "<h3>Rovers</h3>" +
//                         "<p>Los rovers coordinaron todas las actividades y fueron los maestros de ceremonia durante la renovación de promesas.</p>" +
//                         "<h2>Renovación de Promesas</h2>" +
//                         "<p>El momento más emotivo de la jornada fue la renovación de promesas, donde todos los scouts, desde los más pequeños hasta los rovers, reafirmaron su compromiso con:</p>" +
//                         "<ul>" +
//                         "<li>Cumplir sus deberes para con Dios y la Patria</li>" +
//                         "<li>Ayudar al prójimo en toda circunstancia</li>" +
//                         "<li>Cumplir fielmente la Ley Scout</li>" +
//                         "</ul>" +
//                         "<h2>Agradecimientos</h2>" +
//                         "<p>Queremos agradecer especialmente a:</p>" +
//                         "<ul>" +
//                         "<li>Todas las familias que nos acompañaron</li>" +
//                         "<li>Los dirigentes que organizaron cada actividad</li>" +
//                         "<li>Los rovers que coordinaron la logística</li>" +
//                         "<li>La comunidad del barrio que participó activamente</li>" +
//                         "</ul>" +
//                         "<p>¡Seguimos construyendo un mundo mejor, un scout a la vez!</p>")
//                .featuredImage("/media/_MG_0216.JPG")
//                .author(author)
//                .status(NewsArticle.NewsStatus.PUBLISHED)
//                .publishDate(LocalDateTime.now().minusDays(8))
//                .createdAt(LocalDateTime.now().minusDays(9))
//                .updatedAt(LocalDateTime.now().minusDays(8))
//                .viewsCount(156)
//                .categories(eventosCategory != null ? Set.of(eventosCategory) : Set.of())
//                .build()
//        );
//
//        newsArticleRepository.saveAll(articles);
//    }
//
//    private void initializeRoles() {
//        List<Role> roles = Arrays.asList(
//            createRole("FAMILY", "Familia - Acceso a gestión de scouts familiares"),
//            createRole("EDUCATOR", "Educador - Gestión de secciones y aprobación de competencias"),
//            createRole("ADMIN", "Administrador - Acceso completo al sistema"),
//            createRole("NEWS_MANAGER", "Encargado de Noticias - Gestión de contenido y comunicación")
//        );
//        roleRepository.saveAll(roles);
//    }
//
//    private Role createRole(String name, String description) {
//        Role role = new Role();
//        role.setDescription(name);
//        return role;
//    }
//
//    private void initializeSections() {
//        List<Section> sections = Arrays.asList(
//            createSection("Manada", 7, 11),
//            createSection("Scouts", 11, 14),
//            createSection("Caminantes", 14, 17),
//            createSection("Rovers", 17, 21)
//        );
//        sectionRepository.saveAll(sections);
//    }
//
//    private Section createSection(String description, int minAge, int maxAge) {
//        Section section = new Section();
//        section.setDescription(description);
//        section.setMinAge(minAge);
//        section.setMaxAge(maxAge);
//        return section;
//    }
//
//    private void initializeMemberTypes() {
//        List<MemberType> memberTypes = Arrays.asList(
//            createMemberType("Protagonista"),
//            createMemberType("Tutor")
//        );
//        memberTypeRepository.saveAll(memberTypes);
//    }
//
//    private MemberType createMemberType(String description) {
//        MemberType memberType = new MemberType();
//        memberType.setDescription(description);
//        return memberType;
//    }
//
//    private void initializeStatuses() {
//        List<Status> statuses = Arrays.asList(
//            createStatus("Activo"),
//            createStatus("Inactivo"),
//            createStatus("Pendiente")
//        );
//        statusRepository.saveAll(statuses);
//    }
//
//    private Status createStatus(String description) {
//        Status status = new Status();
//        status.setDescription(description);
//        return status;
//    }
//
//    private void initializeUsers() {
//        // Admin
//        User admin = createUser("admin@scoutsjh.org", "González", "admin123");
//        userRepository.save(admin);
//        createUserRole(admin, "ADMIN");
//
//        // Encargado de Noticias
//        User newsManager = createUser("noticias@scoutsjh.org", "Martínez", "news123");
//        userRepository.save(newsManager);
//        createUserRole(newsManager, "NEWS_MANAGER");
//
//        // Educadores
//        User educatorManada = createUser("manada@scoutsjh.org", "López", "edu123");
//        userRepository.save(educatorManada);
//        createUserRole(educatorManada, "EDUCATOR");
//
//        User educatorScouts = createUser("scouts@scoutsjh.org", "Rodríguez", "edu123");
//        userRepository.save(educatorScouts);
//        createUserRole(educatorScouts, "EDUCATOR");
//
//        User educatorCaminantes = createUser("caminantes@scoutsjh.org", "Fernández", "edu123");
//        userRepository.save(educatorCaminantes);
//        createUserRole(educatorCaminantes, "EDUCATOR");
//
//        User educatorRovers = createUser("rovers@scoutsjh.org", "Pérez", "edu123");
//        userRepository.save(educatorRovers);
//        createUserRole(educatorRovers, "EDUCATOR");
//
//        // Familias
//        List<String> familyEmails = Arrays.asList(
//            "familia.garcia@email.com", "familia.silva@email.com", "familia.morales@email.com",
//            "familia.torres@email.com", "familia.romero@email.com", "familia.castro@email.com",
//            "familia.herrera@email.com", "familia.mendoza@email.com"
//        );
//
//        List<String> familyLastNames = Arrays.asList(
//            "García", "Silva", "Morales", "Torres", "Romero", "Castro", "Herrera", "Mendoza"
//        );
//
//        for (int i = 0; i < familyEmails.size(); i++) {
//            User familyUser = createUser(familyEmails.get(i), familyLastNames.get(i), "family123");
//            userRepository.save(familyUser);
//            createUserRole(familyUser, "FAMILY");
//        }
//    }
//
//    private User createUser(String email, String lastName, String password) {
//        User user = new User();
//        user.setEmail(email);
//        user.setLastName(lastName);
//        user.setPasswordHash(passwordEncoder.encode(password));
//        user.setCreatedAt(Instant.now());
//        return user;
//    }
//
//    private void createUserRole(User user, String roleName) {
//        Role role = roleRepository.findAll().stream()
//            .filter(r -> r.getDescription().equals(roleName))
//            .findFirst().orElse(null);
//
//        if (role != null) {
//            RolesXUser rolesXUser = new RolesXUser();
//            rolesXUser.setUser(user);
//            rolesXUser.setRole(role);
//            rolesXUserRepository.save(rolesXUser);
//        }
//    }
//
//    private void initializeFamilyGroups() {
//        List<User> familyUsers = userRepository.findAll().stream()
//            .filter(u -> u.getRolesXUser().stream()
//                .anyMatch(ru -> ru.getRole().getDescription().equals("FAMILY")))
//            .toList();
//
//        MemberType protagonistType = memberTypeRepository.findAll().stream()
//            .filter(mt -> mt.getDescription().equals("Protagonista"))
//            .findFirst().orElse(null);
//
//        MemberType tutorType = memberTypeRepository.findAll().stream()
//            .filter(mt -> mt.getDescription().equals("Tutor"))
//            .findFirst().orElse(null);
//
//        Status activeStatus = statusRepository.findAll().stream()
//            .filter(s -> s.getDescription().equals("Activo"))
//            .findFirst().orElse(null);
//
//        List<Section> sections = sectionRepository.findAll();
//
//        // Crear grupos familiares con miembros
//        String[] familyGroupNames = {
//            "Familia García López", "Familia Silva Morales", "Familia Morales Torres",
//            "Familia Torres Romero", "Familia Romero Castro", "Familia Castro Herrera",
//            "Familia Herrera Mendoza", "Familia Mendoza González"
//        };
//
//        for (int i = 0; i < Math.min(familyUsers.size(), familyGroupNames.length); i++) {
//            User familyUser = familyUsers.get(i);
//
//            // Crear grupo familiar
//            FamilyGroup familyGroup = FamilyGroup.builder()
//                    .name(familyGroupNames[i])
//                .build();
//            familyGroupRepository.save(familyGroup);
//
//            // Crear tutor/tutora
//            Member tutor = createMember(
//                familyUser.getLastName().split(" ")[0],
//                "Carlos",
//                familyUser,
//                tutorType,
//                activeStatus,
//                familyGroup,
//                null,
//                LocalDate.of(1980 + i, 3, 15),
//                "2000000" + (i + 10),
//                true
//            );
//            memberRepository.save(tutor);
//
//            // Crear 1-3 protagonistas por familia
//            int numProtagonistas = 1 + (i % 3); // 1, 2 o 3 protagonistas
//            for (int j = 0; j < numProtagonistas; j++) {
//                String[] nombres = {"Ana", "Juan", "María", "Diego", "Sofia", "Mateo", "Valentina", "Nicolás", "Emma", "Santiago"};
//                int edadBase = 8 + (j * 3) + (i % 4); // Edades variadas entre 8-20
//                Section section = getAppropriateSection(sections, edadBase);
//
//                Member protagonista = createMember(
//                    familyUser.getLastName().split(" ")[0],
//                    nombres[(i * 3 + j) % nombres.length],
//                    null,
//                    protagonistType,
//                    activeStatus,
//                    familyGroup,
//                    section,
//                    LocalDate.of(2024 - edadBase, 6, 10 + j),
//                    "4000000" + (i * 10 + j),
//                    false
//                );
//                memberRepository.save(protagonista);
//            }
//        }
//    }
//
//    private Member createMember(String lastName, String name, User user, MemberType memberType,
//                              Status status, FamilyGroup familyGroup, Section section,
//                              LocalDate birthdate, String dni, boolean isTutor) {
//        return Member.builder()
//            .name(name)
//            .lastname(lastName)
//            .user(user)
//            .memberType(memberType)
//            .status(status)
//            .familyGroup(familyGroup)
//            .section(section)
//            .birthdate(birthdate)
//            .dni(dni)
//            .isTutor(isTutor)
//            .address("Calle Falsa 123, Capital")
//            .email(user != null ? user.getEmail() : null)
//            .contactPhone("261-123-456" + (int)(Math.random() * 10))
//            .accountBalance(BigDecimal.ZERO)
//            .build();
//    }
//
//    private Section getAppropriateSection(List<Section> sections, int age) {
//        return sections.stream()
//            .filter(s -> age >= s.getMinAge() && age <= s.getMaxAge())
//            .findFirst()
//            .orElse(sections.get(0)); // Default to first section if no match
//    }
//
//    private void initializeEducators() {
//        List<User> educators = userRepository.findAll().stream()
//            .filter(u -> u.getRolesXUser().stream()
//                .anyMatch(ru -> ru.getRole().getDescription().equals("EDUCATOR")))
//            .toList();
//
//        List<Section> sections = sectionRepository.findAll();
//
//        // Asignar cada educador a una sección
//        for (int i = 0; i < Math.min(educators.size(), sections.size()); i++) {
//            Educator educator = Educator.builder()
//                .user(educators.get(i))
//                .build();
//            educatorRepository.save(educator);
//
//            // Crear relación educador-sección
//            EducatorsXSection educatorSection = new EducatorsXSection();
//            educatorSection.setEducator(educator);
//            educatorSection.setSection(sections.get(i));
//            // Note: Assuming EducatorsXSectionRepository exists and should be injected
//        }
//    }
//
//    private void initializeFees() {
//        List<Member> protagonistas = memberRepository.findAll().stream()
//            .filter(m -> !m.getIsTutor())
//            .toList();
//
//        // Crear cuotas de los últimos 6 meses
//        YearMonth currentMonth = YearMonth.now();
//
//        for (Member member : protagonistas) {
//            BigDecimal monthlyFee = getMonthlyFeeBySection(member.getSection());
//
//            for (int i = 0; i < 6; i++) {
//                YearMonth feeMonth = currentMonth.minusMonths(i);
//                PaymentStatus status = determineFeeStatus(i);
//
//                Fee fee = Fee.builder()
//                    .description("Cuota mensual " + feeMonth.getMonth().name() + " " + feeMonth.getYear())
//                    .amount(monthlyFee)
//                    .period(feeMonth.toString())
//                    .member(member)
//                    .status(status)
//                    .build();
//
//                feeRepository.save(fee);
//
//                // Si la cuota está paga, actualizar el balance de la cuenta
//                if (status == PaymentStatus.COMPLETED) {
//                    member.setAccountBalance(member.getAccountBalance().subtract(monthlyFee));
//                    memberRepository.save(member);
//                }
//            }
//        }
//    }
//
//    private BigDecimal getMonthlyFeeBySection(Section section) {
//        if (section == null) return new BigDecimal("8000");
//
//        return switch (section.getDescription()) {
//            case "Manada" -> new BigDecimal("6000");
//            case "Scouts" -> new BigDecimal("7000");
//            case "Caminantes" -> new BigDecimal("8000");
//            case "Rovers" -> new BigDecimal("9000");
//            default -> new BigDecimal("7500");
//        };
//    }
//
//    private PaymentStatus determineFeeStatus(int monthsAgo) {
//        // 70% pagas, 20% pendientes, 10% vencidas
//        double random = Math.random();
//        if (monthsAgo <= 2 && random < 0.7) return PaymentStatus.COMPLETED;
//        if (random < 0.9) return PaymentStatus.PENDING;
//        return PaymentStatus.FAILED;
//    }
//
//    private void initializeEvents() {
//        List<Section> sections = sectionRepository.findAll();
//
//        List<Event> events = Arrays.asList(
//            createEvent(
//                "Campamento de Verano 2025",
//                "Gran campamento anual en las sierras de Córdoba para todas las secciones",
//                LocalDateTime.now().plusDays(30),
//                LocalDateTime.now().plusDays(37),
//                new BigDecimal("45000"),
//                EventType.CAMPAMENTO,
//                EventStatus.PUBLISHED,
//                sections
//            ),
//            createEvent(
//                "Fogón de Primavera",
//                "Fogón tradicional con canciones y juegos para toda la familia scout",
//                LocalDateTime.now().plusDays(15),
//                LocalDateTime.now().plusDays(15).plusHours(4),
//                new BigDecimal("2000"),
//                EventType.REUNION,
//                EventStatus.PUBLISHED,
//                sections
//            ),
//            createEvent(
//                "Taller de Nudos - Scouts",
//                "Taller especializado en técnicas de nudos para la sección Scouts",
//                LocalDateTime.now().plusDays(7),
//                LocalDateTime.now().plusDays(7).plusHours(3),
//                BigDecimal.ZERO,
//                EventType.ACTIVIDAD,
//                EventStatus.PUBLISHED,
//                sections.stream().filter(s -> s.getDescription().equals("Scouts")).toList()
//            ),
//            createEvent(
//                "Proyecto Comunitario - Rovers",
//                "Actividad de servicio comunitario en el barrio",
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().minusDays(10).plusHours(8),
//                BigDecimal.ZERO,
//                EventType.SALIDA,
//                EventStatus.COMPLETED,
//                sections.stream().filter(s -> s.getDescription().equals("Rovers")).toList()
//            ),
//            createEvent(
//                "Caminata Ecológica",
//                "Caminata educativa por el Parque San Martín",
//                LocalDateTime.now().minusDays(5),
//                LocalDateTime.now().minusDays(5).plusHours(6),
//                new BigDecimal("1500"),
//                EventType.SALIDA,
//                EventStatus.COMPLETED,
//                sections.stream().filter(s -> !s.getDescription().equals("Rovers")).toList()
//            )
//        );
//
//        eventRepository.saveAll(events);
//
//        // Crear algunas registraciones de ejemplo
//        createEventRegistrations(events);
//    }
//
//    private Event createEvent(String title, String description, LocalDateTime startDate,
//                            LocalDateTime endDate, BigDecimal cost, EventType eventType,
//                            EventStatus status, List<Section> targetSections) {
//        return Event.builder()
//            .title(title)
//            .description(description)
//            .startDate(startDate)
//            .endDate(endDate)
//            .cost(cost)
//            .eventType(eventType)
//            .status(status)
//            .maxCapacity(targetSections.size() > 1 ? 50 : 20)
//            .currentCapacity(0)
//            .requiresPayment(cost.compareTo(BigDecimal.ZERO) > 0)
//            .build();
//    }
//
//    private void createEventRegistrations(List<Event> events) {
//        List<Member> protagonistas = memberRepository.findAll().stream()
//            .filter(m -> !m.getIsTutor())
//            .toList();
//
//        for (Event event : events) {
//            // Registrar 30-70% de los protagonistas en cada evento
//            int registrationCount = (int) (protagonistas.size() * (0.3 + Math.random() * 0.4));
//
//            Collections.shuffle(protagonistas);
//            for (int i = 0; i < Math.min(registrationCount, protagonistas.size()); i++) {
//                Member member = protagonistas.get(i);
//
//                EventRegistration registration = EventRegistration.builder()
//                    .eventId(event.getId())
//                    .memberId(member.getId())
//                    .registrationDate(LocalDateTime.now().minusDays((int)(Math.random() * 20)))
//                    .status(RegistrationStatus.CONFIRMED)
//                    .notes("Registración automática de prueba")
//                    .build();
//
//                eventRegistrationRepository.save(registration);
//            }
//
//            // Actualizar contador de registraciones del evento
//            event.setCurrentCapacity(
//                eventRegistrationRepository.findAllByEventId(event.getId()).size()
//            );
//            eventRepository.save(event);
//        }
//    }
//
//    private void initializeMarchSheets() {
//        List<Member> protagonistas = memberRepository.findAll().stream()
//            .filter(m -> !m.getIsTutor() && m.getSection() != null)
//            .toList();
//
//        List<Competence> competences = competenceRepository.findAll();
//
//        for (Member member : protagonistas) {
//            // Crear hoja de marcha para algunos protagonistas (60%)
//            if (Math.random() < 0.6) {
//                ProgressionStage stage = determineProgressionStage(member);
//
//                MarchSheet marchSheet = MarchSheet.builder()
//                    .member(member)
//                    .progressionStage(stage)
//                    .totem(generateTotem(member.getName()))
//                    .createdAt(LocalDateTime.now().minusDays((int)(Math.random() * 60)).toInstant(java.time.ZoneOffset.UTC))
//                    .build();
//
//                marchSheetRepository.save(marchSheet);
//
//                // Crear progreso en 8-12 competencias seleccionadas aleatoriamente
//                Collections.shuffle(competences);
//                int competenceCount = 8 + (int)(Math.random() * 5); // 8-12 competencias
//
//                for (int i = 0; i < Math.min(competenceCount, competences.size()); i++) {
//                    Competence competence = competences.get(i);
//
//                    CompetenceProgress progress = CompetenceProgress.builder()
//                        .marchSheet(marchSheet)
//                        .competence(competence)
//                        .status(determineCompetenceStatus())
//                        .startDate(LocalDate.now().minusDays((int)(Math.random() * 90)))
//                        .build();
//
//                    // Si está completado o aprobado, agregar fechas y comentarios del educador
//                    if (progress.getStatus() == CompetenceStatus.COMPLETED ||
//                        progress.getStatus() == CompetenceStatus.APPROVED) {
//                        progress.setCompletionDate(LocalDate.now().minusDays((int)(Math.random() * 30)));
//
//                        if (progress.getStatus() == CompetenceStatus.APPROVED) {
//                            progress.setApprovalDate(LocalDate.now().minusDays((int)(Math.random() * 15)));
//                            progress.setEducatorComments("Excelente trabajo demostrado en esta competencia");
//                        }
//                    }
//
//                    competenceProgressRepository.save(progress);
//                }
//            }
//        }
//    }
//
//    private ProgressionStage determineProgressionStage(Member member) {
//        int age = java.time.Period.between(member.getBirthdate(), LocalDate.now()).getYears();
//
//        if (age <= 9) return ProgressionStage.AMBIENTACION;
//        if (age <= 12) return ProgressionStage.TIERRA;
//        if (age <= 15) return ProgressionStage.AIRE;
//        if (age <= 18) return ProgressionStage.FUEGO;
//        return ProgressionStage.AGUA;
//    }
//
//    private String generateTotem(String name) {
//        String[] totems = {
//            "Águila Valiente", "Lobo Astuto", "Oso Fuerte", "Zorro Ágil",
//            "León Corajudo", "Tigre Feroz", "Puma Sigiloso", "Cóndor Majestuoso",
//            "Jaguar Veloz", "Halcón Perspicaz"
//        };
//        return totems[(int)(Math.random() * totems.length)];
//    }
//
//    private CompetenceStatus determineCompetenceStatus() {
//        double random = Math.random();
//        if (random < 0.2) return CompetenceStatus.PENDING;
//        if (random < 0.4) return CompetenceStatus.IN_PROGRESS;
//        if (random < 0.7) return CompetenceStatus.COMPLETED;
//        return CompetenceStatus.APPROVED;
//    }
}}