package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendInvitation(String to, String lastName, String userType, String token) {
        String subject = "Invitación para registrarte en el grupo Scout José Hernández";
        String link = frontendUrl + "/registro?token=" + token;
        
        String userTypeText = "EDUCATOR".equals(userType) ? "educador/a" : "familiar";
        String greeting = "Hola " + lastName + "!";
        
        String text = String.format(
                "%s\n\n" +
                "🎯 Te han invitado a formar parte del Grupo Scout José Hernández como %s.\n\n" +
                "Para completar tu registro, por favor:\n" +
                "1. Hacé clic en el siguiente enlace:\n" +
                "   %s\n\n" +
                "2. Completá tus datos personales\n" +
                "3. Configurá tu contraseña\n\n" +
                "⏰ Este enlace expirará en 72 horas por motivos de seguridad.\n\n" +
                "Si tenés alguna duda, no dudes en contactarnos.\n\n" +
                "¡Esperamos tenerte pronto en nuestra comunidad scout!\n\n" +
                "---\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                greeting, userTypeText, link
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    // Keep backward compatibility method
    public void sendInvitation(String to, String token) {
        sendInvitation(to, "", "FAMILY", token);
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
    
    public void sendNewsNotification(User user, NewsArticle article) {
        String subject = "Nueva noticia - " + article.getTitle();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        String text = String.format(
                "Hola %s!\n\n" +
                "Se ha publicado una nueva noticia en el sitio del Grupo Scout José Hernández:\n\n" +
                "📰 Título: %s\n" +
                "📅 Fecha de publicación: %s\n\n" +
                "📝 Resumen:\n%s\n\n" +
                "Puedes leer el artículo completo en:\n" +
                frontendUrl + "/noticias/%s\n\n" +
                "Saludos,\nGrupo Scout José Hernández",
                user.getLastName(),
                article.getTitle(),
                article.getPublishDate().format(formatter),
                article.getSummary(),
                article.getSlug()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    @Async
    public void sendBulkNewsNotification(List<User> users, NewsArticle article) {
        for (User user : users) {
            try {
                sendNewsNotification(user, article);
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Error enviando email a " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }
    
    public void sendFeeUpdateNotification(User user, java.math.BigDecimal newAmount) {
        String subject = "Actualización de cuotas mensuales - Grupo Scout José Hernández";
        
        String text = String.format(
                "Hola %s!\n\n" +
                "Te informamos que se ha actualizado el valor de las cuotas mensuales del Grupo Scout José Hernández.\n\n" +
                "💰 Nuevo monto mensual: $%.2f\n\n" +
                "Esta actualización afecta a todas las cuotas pendientes de pago de meses anteriores.\n" +
                "Las cuotas ya abonadas no se ven afectadas por este cambio.\n\n" +
                "Podés consultar el estado de tus cuotas y realizar los pagos correspondientes " +
                "ingresando a tu panel familiar en la plataforma.\n\n" +
                "Si tenés alguna consulta sobre esta actualización, no dudes en contactarnos.\n\n" +
                "Gracias por tu comprensión.\n\n" +
                "---\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                user.getLastName(),
                newAmount
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    public void sendMonthlyFeeGenerationNotification(User user, String month, int feesGenerated) {
        String subject = "Nuevas cuotas generadas - " + month;
        
        String text = String.format(
                "Hola %s!\n\n" +
                "Se han generado las cuotas correspondientes al mes %s para los protagonistas de tu familia.\n\n" +
                "📋 Total de cuotas generadas: %d\n\n" +
                "Podés consultar el detalle y realizar el pago de las cuotas ingresando " +
                "a tu panel familiar en la plataforma.\n\n" +
                "Recordá que las cuotas tienen vencimiento mensual y es importante " +
                "mantener los pagos al día para garantizar la participación en todas las actividades.\n\n" +
                "---\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                user.getLastName(),
                month,
                feesGenerated
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    public void sendOverdueFeeReminder(User user, java.util.List<String> overduePeriods, java.math.BigDecimal totalAmount) {
        String subject = "Recordatorio - Cuotas pendientes de pago";
        
        String periodsText = String.join(", ", overduePeriods);
        
        String text = String.format(
                "Hola %s!\n\n" +
                "Te recordamos que tenés cuotas pendientes de pago en el Grupo Scout José Hernández.\n\n" +
                "📅 Períodos con cuotas pendientes: %s\n" +
                "💰 Monto total adeudado: $%.2f\n\n" +
                "Para mantener la participación activa de los protagonistas en todas las actividades " +
                "del grupo, es importante regularizar los pagos pendientes.\n\n" +
                "Podés realizar el pago de las cuotas ingresando a tu panel familiar " +
                "en la plataforma o contactándonos para coordinar otras formas de pago.\n\n" +
                "Si ya realizaste el pago, por favor ignorá este mensaje.\n\n" +
                "---\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                user.getLastName(),
                periodsText,
                totalAmount
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    public void sendPasswordResetEmail(String to, String lastName, String resetToken) {
        String subject = "Recuperación de contraseña - Grupo Scout José Hernández";
        String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken;
        
        String text = String.format(
                "Hola %s!\n\n" +
                "🔐 Has solicitado restablecer tu contraseña en el sistema del Grupo Scout José Hernández.\n\n" +
                "Para crear una nueva contraseña, por favor:\n" +
                "1. Hacé clic en el siguiente enlace:\n" +
                "   %s\n\n" +
                "2. Ingresá tu nueva contraseña\n" +
                "3. Confirmá los cambios\n\n" +
                "⏰ Este enlace expirará en 1 hora por motivos de seguridad.\n\n" +
                "Si no solicitaste este cambio, podés ignorar este mensaje de forma segura. " +
                "Tu contraseña actual permanecerá sin cambios.\n\n" +
                "Si tenés problemas para acceder o necesitás ayuda, no dudes en contactarnos.\n\n" +
                "---\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                lastName, resetLink
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    /**
     * Send notification to admin when a new contact message is received
     */
    @Async
    public void sendContactNotificationToAdmin(ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessage contactMessage) {
        String subject = "Nuevo mensaje de contacto - " + contactMessage.getSubject();
        
        String text = String.format(
                "Se ha recibido un nuevo mensaje de contacto en el sitio web del Grupo Scout José Hernández.\n\n" +
                "📋 DETALLES DEL MENSAJE:\n" +
                "👤 Nombre: %s\n" +
                "📧 Email: %s\n" +
                "📝 Asunto: %s\n" +
                "🏷️ Tipo: %s\n" +
                "📅 Fecha: %s\n\n" +
                "💬 MENSAJE:\n%s\n\n" +
                "---\n" +
                "Para gestionar este mensaje, ingresa al panel de administración:\n" +
                frontendUrl + "/admin/contact\n\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                contactMessage.getName(),
                contactMessage.getEmail(),
                contactMessage.getSubject(),
                contactMessage.getMessageType().toString(),
                contactMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                contactMessage.getMessage()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo("gsjosehernandez378@gmail.com"); // Admin email
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    /**
     * Send auto-reply confirmation to user when they submit a contact form
     */
    @Async
    public void sendContactAutoReply(ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessage contactMessage) {
        String subject = "Mensaje recibido - Grupo Scout José Hernández";
        
        String text = String.format(
                "Hola %s!\n\n" +
                "Hemos recibido tu mensaje de contacto y queremos agradecerte por comunicarte con nosotros.\n\n" +
                "📋 RESUMEN DE TU CONSULTA:\n" +
                "📝 Asunto: %s\n" +
                "📅 Fecha de envío: %s\n" +
                "🆔 Número de referencia: #%d\n\n" +
                "Nuestro equipo revisará tu consulta y te responderemos a la brevedad posible, " +
                "generalmente dentro de las próximas 24 a 48 horas.\n\n" +
                "Si tu consulta es urgente, también podés contactarnos a través de:\n" +
                "📧 Email: gsjosehernandez378@gmail.com\n" +
                "📱 Instagram: @josehernandez378\n" +
                "📍 Dirección: Nuflo de Aguilar 562, Barrio Marqués de Sobremonte, Córdoba\n" +
                "🕐 Horarios de reunión: Sábados de 14:30 a 18:30 hs\n\n" +
                "¡Gracias por tu interés en el Grupo Scout José Hernández!\n\n" +
                "---\n" +
                "Grupo Scout José Hernández\n" +
                "Sistema de Gestión Scout",
                contactMessage.getName(),
                contactMessage.getSubject(),
                contactMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                contactMessage.getId()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tomeix13@gmail.com");
        message.setTo(contactMessage.getEmail());
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
