package ar.edu.utn.frc.sistemascoutsjosehernandez.util;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
