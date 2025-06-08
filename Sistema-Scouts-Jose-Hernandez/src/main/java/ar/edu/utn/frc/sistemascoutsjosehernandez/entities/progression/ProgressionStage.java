package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression;

public enum ProgressionStage {
    AMBIENTACION("Ambientación"),
    TIERRA("Tierra"),
    AIRE("Aire"),
    FUEGO("Fuego"),
    AGUA("Agua");

    private final String displayName;

    ProgressionStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}