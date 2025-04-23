package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
