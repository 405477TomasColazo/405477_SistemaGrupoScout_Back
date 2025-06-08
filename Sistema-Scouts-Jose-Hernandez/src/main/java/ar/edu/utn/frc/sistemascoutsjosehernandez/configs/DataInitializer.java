package ar.edu.utn.frc.sistemascoutsjosehernandez.configs;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.Competence;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.SuggestedAction;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.CompetenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CompetenceRepository competenceRepository;

    @Override
    public void run(String... args) {
        if (competenceRepository.count() == 0) {
            log.info("Cargando competencias de ejemplo...");
            initializeCompetences();
            log.info("Competencias de ejemplo cargadas exitosamente.");
        } else {
            log.info("Las competencias ya están cargadas en la base de datos.");
        }
    }

    private void initializeCompetences() {
        List<Competence> competences = Arrays.asList(
            // SALUD Y BIENESTAR
            createCompetence(
                "Adopto y promuevo hábitos de alimentación e higiene saludables",
                "Desarrollo hábitos saludables de alimentación e higiene personal que contribuyen a mi bienestar físico y mental.",
                GrowthArea.HEALTH_WELLBEING,
                Arrays.asList(
                    "Mantengo el ambiente limpio y ordenado, en mi hogar, en mi espacio de reunión con el equipo",
                    "Sostengo una alimentación equilibrada que incluya variedad de alimentos con recetas saludables",
                    "Practico hábitos de higiene personal diarios"
                ),
                Arrays.asList(
                    "¿Por qué debe importarme lo que como?",
                    "¿Cuál es la importancia de aprender a cocinar?",
                    "¿Cómo impactan mis hábitos de higiene en mi relación con otros?"
                )
            ),
            createCompetence(
                "Cuido mi cuerpo desarrollando destrezas físicas",
                "Mantengo una vida activa desarrollando habilidades físicas y deportivas que fortalezcan mi cuerpo.",
                GrowthArea.HEALTH_WELLBEING,
                Arrays.asList(
                    "Practico regularmente algún deporte o actividad física",
                    "Participo en juegos y actividades que desarrollen mi coordinación",
                    "Aprendo técnicas de relajación y manejo del estrés"
                ),
                Arrays.asList(
                    "¿Qué beneficios tiene para mí mantenerme activo físicamente?",
                    "¿Cómo puedo equilibrar el tiempo de estudio con la actividad física?"
                )
            ),
            createCompetence(
                "Reconozco mis emociones y las expreso de manera positiva",
                "Desarrollo inteligencia emocional para reconocer, comprender y expresar mis sentimientos de manera constructiva.",
                GrowthArea.HEALTH_WELLBEING,
                Arrays.asList(
                    "Identifico y nombro mis emociones cuando las experimento",
                    "Busco formas saludables de expresar lo que siento",
                    "Pido ayuda cuando me siento abrumado emocionalmente"
                ),
                Arrays.asList(
                    "¿Qué emociones me resultan más difíciles de manejar?",
                    "¿Cómo puedo comunicar mejor lo que siento a mi familia y amigos?"
                )
            ),

            // PAZ Y DESARROLLO
            createCompetence(
                "Contribuyo al desarrollo de mi comunidad",
                "Participo activamente en proyectos y actividades que beneficien a mi comunidad y promuevan el bien común.",
                GrowthArea.PEACE_DEVELOPMENT,
                Arrays.asList(
                    "Participo en proyectos de servicio comunitario",
                    "Colaboro con organizaciones locales en actividades solidarias",
                    "Promuevo valores de respeto y convivencia en mi entorno"
                ),
                Arrays.asList(
                    "¿Qué problemas veo en mi comunidad que podría ayudar a resolver?",
                    "¿Cómo puedo involucrar a otros jóvenes en actividades de servicio?"
                )
            ),
            createCompetence(
                "Promuevo la paz y resuelvo conflictos de manera constructiva",
                "Desarrollo habilidades para mediar conflictos y promover la convivencia pacífica en mis relaciones.",
                GrowthArea.PEACE_DEVELOPMENT,
                Arrays.asList(
                    "Aprendo técnicas de mediación y resolución de conflictos",
                    "Practico la escucha activa cuando hay desacuerdos",
                    "Busco soluciones que beneficien a todas las partes involucradas"
                ),
                Arrays.asList(
                    "¿Qué estrategias uso actualmente para resolver conflictos?",
                    "¿Cómo puedo ser un agente de paz en mi familia y grupo de amigos?"
                )
            ),
            createCompetence(
                "Valoro y respeto la diversidad cultural",
                "Reconozco y celebro las diferencias culturales, promoviendo la inclusión y el respeto mutuo.",
                GrowthArea.PEACE_DEVELOPMENT,
                Arrays.asList(
                    "Aprendo sobre diferentes culturas y tradiciones",
                    "Participo en eventos multiculturales",
                    "Defiendo los derechos de personas de diferentes orígenes"
                ),
                Arrays.asList(
                    "¿Qué puedo aprender de culturas diferentes a la mía?",
                    "¿Cómo puedo combatir los prejuicios y estereotipos?"
                )
            ),

            // AMBIENTE
            createCompetence(
                "Cuido y protejo el medio ambiente",
                "Desarrollo conciencia ambiental y prácticas sostenibles que contribuyan a la protección del planeta.",
                GrowthArea.ENVIRONMENT,
                Arrays.asList(
                    "Implemento prácticas de reciclaje en mi hogar",
                    "Reduzco mi consumo de recursos naturales",
                    "Participo en actividades de conservación ambiental"
                ),
                Arrays.asList(
                    "¿Qué acciones concretas puedo tomar para reducir mi impacto ambiental?",
                    "¿Cómo puedo involucrar a mi familia en prácticas más sostenibles?"
                )
            ),
            createCompetence(
                "Promuevo el uso responsable de recursos naturales",
                "Utilizo de manera consciente y responsable los recursos naturales, promoviendo su conservación.",
                GrowthArea.ENVIRONMENT,
                Arrays.asList(
                    "Ahorro agua y energía en mis actividades diarias",
                    "Elijo productos eco-amigables cuando es posible",
                    "Educo a otros sobre la importancia del uso responsable de recursos"
                ),
                Arrays.asList(
                    "¿Cuántos recursos naturales consumo diariamente?",
                    "¿Qué alternativas sostenibles puedo implementar en mi vida?"
                )
            ),
            createCompetence(
                "Participo en la protección de espacios naturales",
                "Contribuyo activamente a la conservación y protección de espacios naturales en mi entorno.",
                GrowthArea.ENVIRONMENT,
                Arrays.asList(
                    "Participo en jornadas de limpieza de espacios naturales",
                    "Aprendo sobre la flora y fauna local",
                    "Promuevo el respeto por los espacios verdes urbanos"
                ),
                Arrays.asList(
                    "¿Qué espacios naturales cerca de mi hogar necesitan protección?",
                    "¿Cómo puedo contribuir a crear más áreas verdes en mi comunidad?"
                )
            ),

            // HABILIDADES PARA LA VIDA
            createCompetence(
                "Desarrollo habilidades de liderazgo",
                "Aprendo a liderar equipos y proyectos, inspirando a otros a trabajar hacia objetivos comunes.",
                GrowthArea.LIFE_SKILLS,
                Arrays.asList(
                    "Lidero un proyecto en mi equipo scout",
                    "Organizo actividades para mi grupo de amigos",
                    "Tomo iniciativa para resolver problemas grupales"
                ),
                Arrays.asList(
                    "¿Qué cualidades tiene un buen líder?",
                    "¿Cómo puedo inspirar a otros a participar en proyectos positivos?"
                )
            ),
            createCompetence(
                "Gestiono mi tiempo y organizo mis actividades",
                "Desarrollo habilidades de planificación y organización para equilibrar mis responsabilidades y actividades.",
                GrowthArea.LIFE_SKILLS,
                Arrays.asList(
                    "Uso una agenda para planificar mis actividades",
                    "Establecer prioridades en mis tareas diarias",
                    "Equilibro tiempo de estudio, descanso y recreación"
                ),
                Arrays.asList(
                    "¿Cómo puedo aprovechar mejor mi tiempo?",
                    "¿Qué actividades son más importantes para mi desarrollo personal?"
                )
            ),
            createCompetence(
                "Desarrollo habilidades de comunicación efectiva",
                "Aprendo a comunicarme de manera clara, asertiva y empática en diferentes contextos.",
                GrowthArea.LIFE_SKILLS,
                Arrays.asList(
                    "Practico hablar en público con confianza",
                    "Aprendo a escuchar activamente a otros",
                    "Expreso mis ideas de manera clara y respetuosa"
                ),
                Arrays.asList(
                    "¿Qué barreras encuentro para comunicarme efectivamente?",
                    "¿Cómo puedo mejorar mi capacidad de escuchar a otros?"
                )
            ),
            createCompetence(
                "Tomo decisiones responsables e informadas",
                "Desarrollo la capacidad de tomar decisiones reflexivas considerando sus consecuencias e impacto.",
                GrowthArea.LIFE_SKILLS,
                Arrays.asList(
                    "Analizo las opciones antes de tomar decisiones importantes",
                    "Considero las consecuencias de mis acciones",
                    "Busco información y consejos cuando necesito decidir"
                ),
                Arrays.asList(
                    "¿Qué proceso sigo para tomar decisiones importantes?",
                    "¿Cómo puedo aprender de las decisiones que no salieron como esperaba?"
                )
            )
        );

        competenceRepository.saveAll(competences);
    }

    private Competence createCompetence(String title, String description, GrowthArea growthArea, 
                                      List<String> suggestedActionTexts, List<String> guidingQuestions) {
        Competence competence = Competence.builder()
                .title(title)
                .description(description)
                .growthArea(growthArea)
                .guidingQuestions(guidingQuestions)
                .build();

        // Crear acciones sugeridas
        List<SuggestedAction> suggestedActions = suggestedActionTexts.stream()
                .map(text -> SuggestedAction.builder()
                        .description(text)
                        .competence(competence)
                        .build())
                .toList();

        competence.setSuggestedActions(suggestedActions);
        return competence;
    }
}