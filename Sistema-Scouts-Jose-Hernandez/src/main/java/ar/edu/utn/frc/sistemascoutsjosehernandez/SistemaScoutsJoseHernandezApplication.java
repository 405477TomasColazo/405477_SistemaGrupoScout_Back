package ar.edu.utn.frc.sistemascoutsjosehernandez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SistemaScoutsJoseHernandezApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaScoutsJoseHernandezApplication.class, args);
    }

}
