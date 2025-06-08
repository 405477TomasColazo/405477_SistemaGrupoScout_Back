package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendInvitation(String to, String token) {
        String subject = "Invitación para registrarte en el grupo Scout Jose Hernandez";
        String link = "http://localhost:4200/registro?token=" + token;
        String text = "Hola!\n\nTe han invitado a registrarte en la plataforma. " +
                "Por favor hacé clic en el siguiente enlace para completar tu registro:\n\n" + link +
                "\n\nEste enlace expirará en 72 horas.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    public void sendEventInvitation(User user, Event event) {
        String subject = "Invitación a evento - " + event.getTitle();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        String text = String.format(
                "Hola %s!\n\n" +
                "Has sido invitado al siguiente evento:\n\n" +
                "📅 Evento: %s\n" +
                "📍 Ubicación: %s\n" +
                "🕐 Fecha de inicio: %s\n" +
                "🕐 Fecha de fin: %s\n" +
                "💰 Costo: $%.2f\n\n" +
                "Descripción:\n%s\n\n" +
                "Por favor, confirma tu participación en la plataforma.\n\n" +
                "Saludos,\nGrupo Scout José Hernández",
                user.getLastName(),
                event.getTitle(),
                event.getLocation() != null ? event.getLocation() : "Por definir",
                event.getStartDate().format(formatter),
                event.getEndDate() != null ? event.getEndDate().format(formatter) : "Por definir",
                event.getCost() != null ? event.getCost() : 0.0,
                event.getDescription() != null ? event.getDescription() : "Sin descripción"
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    public void sendEventUpdateNotification(User user, Event event) {
        String subject = "Evento actualizado - " + event.getTitle();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        String text = String.format(
                "Hola %s!\n\n" +
                "El evento '%s' ha sido actualizado.\n\n" +
                "Por favor revisa los detalles en la plataforma:\n" +
                "📅 Fecha de inicio: %s\n" +
                "📍 Ubicación: %s\n\n" +
                "Saludos,\nGrupo Scout José Hernández",
                user.getLastName(),
                event.getTitle(),
                event.getStartDate().format(formatter),
                event.getLocation() != null ? event.getLocation() : "Por definir"
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
