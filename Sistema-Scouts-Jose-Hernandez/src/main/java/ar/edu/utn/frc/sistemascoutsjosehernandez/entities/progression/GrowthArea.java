package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression;

public enum GrowthArea {
    PEACE_DEVELOPMENT("Paz y Desarrollo"),
    HEALTH_WELLBEING("Salud y Bienestar"),
    ENVIRONMENT("Ambiente"),
    LIFE_SKILLS("Habilidades para la Vida");

    private final String displayName;

    GrowthArea(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}