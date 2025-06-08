package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum InvitationType {
    ALL("all"),
    SELECTED("selected");

    private final String value;

    InvitationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String toValue() {
        return this.value;
    }

    @JsonCreator
    public static InvitationType fromValue(String value) {
        for (InvitationType type : InvitationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid InvitationType: " + value);
    }
}
