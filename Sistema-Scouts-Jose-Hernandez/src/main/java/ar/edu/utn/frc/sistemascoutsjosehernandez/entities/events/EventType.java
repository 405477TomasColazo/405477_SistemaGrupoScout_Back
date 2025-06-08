package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventType {
    CAMPAMENTO("campamento"),
    SALIDA("salida"),
    REUNION("reunion"),
    ACTIVIDAD("actividad"),
    OTRO("otro");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String toValue() {
        return this.value;
    }

    @JsonCreator
    public static EventType fromValue(String value) {
        for (EventType type : EventType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid EventType: " + value);
    }
}
