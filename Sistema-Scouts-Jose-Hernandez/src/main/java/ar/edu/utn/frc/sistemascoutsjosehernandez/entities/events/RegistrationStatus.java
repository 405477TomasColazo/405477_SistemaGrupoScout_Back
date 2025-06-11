package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RegistrationStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    WAITLIST("waitlist"),
    DECLINED("declined");

    private final String value;

    RegistrationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String toValue() {
        return this.value;
    }

    @JsonCreator
    public static RegistrationStatus fromValue(String value) {
        for (RegistrationStatus status : RegistrationStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid RegistrationStatus: " + value);
    }
}

