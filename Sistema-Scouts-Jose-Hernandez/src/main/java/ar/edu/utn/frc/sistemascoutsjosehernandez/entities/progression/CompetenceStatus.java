package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression;

public enum CompetenceStatus {
    PENDING("Pendiente"),
    IN_PROGRESS("En Progreso"),
    COMPLETED("Completada"),
    APPROVED("Aprobada");

    private final String displayName;

    CompetenceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}