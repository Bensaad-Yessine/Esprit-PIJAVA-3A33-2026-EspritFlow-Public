package piJava.services;

import piJava.entities.Seance;
import piJava.entities.user;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "yassinebensaad567@gmail.com";
    private static final String SMTP_PASSWORD = "moguhlqafuqzxefp";

    public static void sendSeanceNotificationAsync(String action, Seance seance, String className, String salleName, List<user> recipients) {
        if (recipients == null || recipients.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USERNAME, "Scolarité Esprit"));
                
                // Add all valid users to BCC to protect their privacy
                int validRecipients = 0;
                System.out.println("Vérification de " + recipients.size() + " étudiants potentiels...");
                for (user u : recipients) {
                    System.out.println("  -> Etudiant: " + u.getEmail() + " | Roles: " + u.getRoles());
                    if (u.getEmail() != null && !u.getEmail().trim().isEmpty()) {
                        if (u.getRoles() == null || !u.getRoles().toLowerCase().contains("admin")) {
                            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(u.getEmail()));
                            validRecipients++;
                            System.out.println("     [OK] Ajouté à la liste.");
                        } else {
                            System.out.println("     [IGNORE] L'utilisateur est un admin.");
                        }
                    } else {
                        System.out.println("     [IGNORE] Email invalide ou null.");
                    }
                }

                if (validRecipients == 0) {
                    System.out.println("Aucun destinataire valide pour l'email.");
                    return;
                }

                message.setSubject(action + " de séance - Classe " + className);

                // Format dates
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String debut = seance.getHeureDebut() != null ? seance.getHeureDebut().toLocalDateTime().format(formatter) : "N/A";
                String fin = seance.getHeureFin() != null ? seance.getHeureFin().toLocalDateTime().format(formatter) : "N/A";

                String dayOfWeek = seance.getHeureDebut() != null ? 
                    seance.getHeureDebut().toLocalDateTime().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) : "N/A";

                if (dayOfWeek != null && dayOfWeek.length() > 0) {
                    dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1);
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Action: ").append(action).append("\n");
                sb.append("Jour: ").append(dayOfWeek).append("\n");
                sb.append("Type: ").append(seance.getTypeSeance() != null ? seance.getTypeSeance() : "N/A").append("\n");
                sb.append("Mode: ").append(seance.getMode() != null ? seance.getMode() : "N/A").append("\n");
                sb.append("Classe: ").append(className).append("\n");
                sb.append("Salle: ").append(salleName != null ? salleName : "N/A").append("\n");
                sb.append("Début: ").append(debut).append("\n");
                sb.append("Fin: ").append(fin).append("\n");

                message.setText(sb.toString());

                Transport.send(message);
                System.out.println("Email envoyé avec succès pour l'action: " + action + " à " + validRecipients + " étudiants.");

            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
