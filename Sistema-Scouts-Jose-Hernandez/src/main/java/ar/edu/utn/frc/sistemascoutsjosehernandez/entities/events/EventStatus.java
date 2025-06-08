package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String toValue() {
        return this.value;
    }

    @JsonCreator
    public static EventStatus fromValue(String value) {
        for (EventStatus status : EventStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid EventStatus: " + value);
    }
}
