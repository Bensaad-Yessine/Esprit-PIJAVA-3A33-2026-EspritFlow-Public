# 📧 Documentation Fonctionnalité : Système de Mailing

## 📋 Vue d'ensemble

Le système de mailing permet d'envoyer des notifications par email aux étudiants et enseignants concernant les séances, changements d'emploi du temps, rappels et autres communications importantes.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **Bibliothèques Java**
- **JavaMail API (javax.mail)** : Envoi d'emails
  - `javax.mail.Session` : Session de connexion SMTP
  - `javax.mail.Message` : Message email
  - `javax.mail.Transport` : Envoi du message
  - `javax.mail.internet.MimeMessage` : Email MIME
  - `javax.mail.internet.InternetAddress` : Adresses email

### 2. **Protocoles**
- **SMTP (Simple Mail Transfer Protocol)** : Protocole d'envoi
- **TLS/SSL** : Chiffrement sécurisé
- **MIME** : Multipurpose Internet Mail Extensions (pièces jointes, HTML)

### 3. **Templates**
- **Template Engine** (optionnel) : FreeMarker, Thymeleaf
- **HTML/CSS** : Mise en forme des emails
- **Placeholders** : Variables dynamiques dans les templates

---

## 🔧 Architecture du Système

### 1. **Schéma de Base de Données**

```sql
-- Table pour stocker les logs d'emails envoyés
CREATE TABLE email_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    body TEXT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS',
    error_message TEXT
);

-- Table pour les préférences de notification
CREATE TABLE notification_preferences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    reminder_hours_before INT DEFAULT 24,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

---

### 2. **Flux de Traitement des Emails**

```
Déclencheur (Event)
    ↓
Service Mailing
    ↓
Récupération Destinataire(s)
    ↓
Génération du Template
    ↓
Remplacement des Variables
    ↓
Création du Message (MimeMessage)
    ↓
Configuration SMTP
    ↓
Envoi via Transport
    ↓
Log du Résultat
```

---

## 💻 Implémentation Technique

### 1. **Configuration SMTP**

```java
import java.util.Properties;
import javax.mail.Session;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Classe de configuration SMTP
 */
public class SMTPConfig {
    
    // Configuration pour Gmail (exemple)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;  // Port TLS
    private static final String USERNAME = "espritflow.noreply@gmail.com";
    private static final String PASSWORD = "votre_mot_de_passe_application";
    
    /**
     * Crée une Session SMTP sécurisée
     */
    public static Session createSMTPSession() {
        
        Properties props = new Properties();
        
        // Configuration du serveur SMTP
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        // Activer l'authentification
        props.put("mail.smtp.auth", "true");
        
        // Activer TLS
        props.put("mail.smtp.starttls.enable", "true");
        
        // Timeout de connexion (en millisecondes)
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        
        // Créer l'authentificateur
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        };
        
        // Créer la session
        Session session = Session.getInstance(props, authenticator);
        
        // Activer le debug (optionnel)
        // session.setDebug(true);
        
        return session;
    }
}
```

**Tableau de configuration par fournisseur :**

| Fournisseur | Host | Port | Sécurité |
|-------------|-------|-------|-----------|
| Gmail | smtp.gmail.com | 587 (TLS) / 465 (SSL) | STARTTLS |
| Outlook | smtp.office365.com | 587 | STARTTLS |
| Yahoo | smtp.mail.yahoo.com | 587 | STARTTLS |
| SendGrid | smtp.sendgrid.net | 587 | STARTTLS |

---

### 2. **Classe de Service Mailing**

```java
import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;

/**
 * Service principal pour l'envoi d'emails
 */
public class MailingService {
    
    private Session smtpSession;
    private EmailLogService emailLogService;
    
    public MailingService() {
        this.smtpSession = SMTPConfig.createSMTPSession();
        this.emailLogService = new EmailLogService();
    }
    
    /**
     * Envoie un email simple
     * 
     * @param to Adresse du destinataire
     * @param subject Sujet de l'email
     * @param body Corps de l'email (HTML ou texte)
     * @return true si envoyé avec succès
     */
    public boolean sendEmail(String to, String subject, String body) {
        
        return sendEmail(new String[]{to}, subject, body, null);
    }
    
    /**
     * Envoie un email à plusieurs destinataires
     * 
     * @param to Adresses des destinataires
     * @param subject Sujet
     * @param body Corps
     * @param attachments Pièces jointes (optionnel)
     * @return true si succès
     */
    public boolean sendEmail(String[] to, String subject, String body, 
                          List<Attachment> attachments) {
        
        try {
            // Créer le message
            MimeMessage message = new MimeMessage(smtpSession);
            
            // Expéditeur
            message.setFrom(new InternetAddress(SMTPConfig.USERNAME, "EspritFlow"));
            
            // Destinataires (TO)
            InternetAddress[] toAddresses = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                toAddresses[i] = new InternetAddress(to[i]);
            }
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            
            // Sujet
            message.setSubject(subject, "UTF-8");
            
            // Corps du message
            if (isHTML(body)) {
                message.setContent(body, "text/html; charset=UTF-8");
            } else {
                message.setText(body, "UTF-8");
            }
            
            // Pièces jointes
            if (attachments != null && !attachments.isEmpty()) {
                addAttachments(message, body, attachments);
            }
            
            // Envoi
            Transport.send(message);
            
            // Log du succès
            emailLogService.logEmail(to, subject, "SUCCESS", null);
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            
            // Log de l'échec
            emailLogService.logEmail(to, subject, "FAILED", e.getMessage());
            
            return false;
        }
    }
    
    /**
     * Vérifie si le corps est en HTML
     */
    private boolean isHTML(String body) {
        return body != null && 
               (body.contains("<html>") || body.contains("<div>") || 
                body.contains("<p>") || body.contains("<body>"));
    }
}
```

---

### 3. **Templates d'Email**

```java
import java.util.Map;

/**
 * Gestionnaire de templates d'emails
 */
public class EmailTemplateManager {
    
    /**
     * Template pour notification de nouvelle séance
     */
    public static String getNewSeanceTemplate(Map<String, String> data) {
        
        String template = 
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <style>" +
            "    body { font-family: Arial, sans-serif; background-color: #f5f5f5; }" +
            "    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; }" +
            "    .header { background: #ef4444; color: white; padding: 20px; text-align: center; }" +
            "    .content { padding: 20px; }" +
            "    .info-box { background: #fee2e2; padding: 15px; margin: 10px 0; border-left: 4px solid #ef4444; }" +
            "    .footer { text-align: center; padding: 20px; color: #666; }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class='container'>" +
            "    <div class='header'>" +
            "      <h1>🎓 Nouvelle Séance Ajoutée</h1>" +
            "    </div>" +
            "    <div class='content'>" +
            "      <p>Bonjour <strong>{{prenom}} {{nom}}</strong>,</p>" +
            "      <p>Une nouvelle séance a été ajoutée à votre emploi du temps :</p>" +
            "      <div class='info-box'>" +
            "        <p><strong>Matière :</strong> {{matiere}}</p>" +
            "        <p><strong>Type :</strong> {{type}}</p>" +
            "        <p><strong>Jour :</strong> {{jour}}</p>" +
            "        <p><strong>Horaires :</strong> {{heure_debut}} - {{heure_fin}}</p>" +
            "        <p><strong>Salle :</strong> {{salle}}</p>" +
            "      </div>" +
            "      <p>Veuillez consulter votre emploi du temps sur EspritFlow.</p>" +
            "    </div>" +
            "    <div class='footer'>" +
            "      <p>Cet email est envoyé automatiquement. Ne répondez pas.</p>" +
            "      <p>© 2026 EspritFlow</p>" +
            "    </div>" +
            "  </div>" +
            "</body>" +
            "</html>";
        
        // Remplacer les variables
        for (Map.Entry<String, String> entry : data.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        
        return template;
    }
    
    /**
     * Template pour rappel de séance
     */
    public static String getReminderTemplate(Map<String, String> data) {
        
        String template = 
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <style>" +
            "    body { font-family: Arial, sans-serif; background-color: #fef3c7; }" +
            "    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; }" +
            "    .header { background: #f59e0b; color: white; padding: 20px; text-align: center; }" +
            "    .alert { background: #fef3c7; border: 2px solid #f59e0b; padding: 15px; margin: 20px 0; text-align: center; }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class='container'>" +
            "    <div class='header'>" +
            "      <h1>⏰ Rappel de Séance</h1>" +
            "    </div>" +
            "    <div class='alert'>" +
            "      <h2>Dans {{hours_before}} heures</h2>" +
            "    </div>" +
            "    <p>Bonjour <strong>{{prenom}} {{nom}}</strong>,</p>" +
            "    <p>N'oubliez pas votre séance :</p>" +
            "    <ul>" +
            "      <li><strong>Matière :</strong> {{matiere}}</li>" +
            "      <li><strong>Heure :</strong> {{heure_debut}}</li>" +
            "      <li><strong>Salle :</strong> {{salle}}</li>" +
            "    </ul>" +
            "    <p>À très bientôt !</p>" +
            "  </div>" +
            "</body>" +
            "</html>";
        
        for (Map.Entry<String, String> entry : data.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        
        return template;
    }
}
```

---

### 4. **Utilisation des Templates**

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour envoyer des notifications de séance
 */
public class SeanceNotificationService {
    
    private MailingService mailingService;
    
    public SeanceNotificationService() {
        this.mailingService = new MailingService();
    }
    
    /**
     * Notifie les étudiants d'une nouvelle séance
     */
    public void notifyNewSeance(Seance seance, List<user> students) {
        
        for (user student : students) {
            
            // Vérifier les préférences de notification
            if (!student.wantsEmailNotification()) {
                continue;
            }
            
            // Préparer les données pour le template
            Map<String, String> data = new HashMap<>();
            data.put("prenom", student.getPrenom());
            data.put("nom", student.getNom());
            data.put("matiere", getMatiereName(seance.getMatiereId()));
            data.put("type", seance.getTypeSeance());
            data.put("jour", seance.getJour());
            data.put("heure_debut", formatTime(seance.getHeureDebut()));
            data.put("heure_fin", formatTime(seance.getHeureFin()));
            data.put("salle", getSalleName(seance.getSalleId()));
            
            // Générer l'email
            String subject = "Nouvelle séance : " + getMatiereName(seance.getMatiereId());
            String body = EmailTemplateManager.getNewSeanceTemplate(data);
            
            // Envoyer
            mailingService.sendEmail(student.getEmail(), subject, body);
        }
    }
    
    /**
     * Envoie un rappel X heures avant la séance
     */
    public void sendReminder(Seance seance, List<user> students, int hoursBefore) {
        
        for (user student : students) {
            
            Map<String, String> data = new HashMap<>();
            data.put("prenom", student.getPrenom());
            data.put("nom", student.getNom());
            data.put("matiere", getMatiereName(seance.getMatiereId()));
            data.put("heure_debut", formatTime(seance.getHeureDebut()));
            data.put("salle", getSalleName(seance.getSalleId()));
            data.put("hours_before", String.valueOf(hoursBefore));
            
            String subject = "Rappel : Séance dans " + hoursBefore + "h";
            String body = EmailTemplateManager.getReminderTemplate(data);
            
            mailingService.sendEmail(student.getEmail(), subject, body);
        }
    }
}
```

---

### 5. **Pièces Jointes**

```java
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * Classe pour les pièces jointes
 */
public class Attachment {
    
    private String fileName;
    private byte[] content;
    private String mimeType;
    
    public Attachment(String fileName, byte[] content, String mimeType) {
        this.fileName = fileName;
        this.content = content;
        this.mimeType = mimeType;
    }
    
    // Getters
    public String getFileName() { return fileName; }
    public byte[] getContent() { return content; }
    public String getMimeType() { return mimeType; }
}

/**
 * Ajoute des pièces jointes au message
 */
private void addAttachments(MimeMessage message, String body, 
                          List<Attachment> attachments) throws Exception {
    
    // Créer une partie multipart
    MimeMultipart multipart = new MimeMultipart();
    
    // Partie corps du message
    MimeBodyPart bodyPart = new MimeBodyPart();
    if (isHTML(body)) {
        bodyPart.setContent(body, "text/html; charset=UTF-8");
    } else {
        bodyPart.setText(body, "UTF-8");
    }
    multipart.addBodyPart(bodyPart);
    
    // Partie pièces jointes
    for (Attachment attachment : attachments) {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        
        // DataSource pour le contenu
        DataSource source = new ByteArrayDataSource(
            attachment.getContent(),
            attachment.getMimeType()
        );
        
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(attachment.getFileName());
        
        multipart.addBodyPart(attachmentPart);
    }
    
    // Ajouter le multipart au message
    message.setContent(multipart);
}

/**
 * DataSource personnalisé pour les bytes en mémoire
 */
class ByteArrayDataSource implements DataSource {
    
    private byte[] data;
    private String type;
    
    public ByteArrayDataSource(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }
    
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
    
    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getContentType() {
        return type;
    }
    
    @Override
    public String getName() {
        return "ByteArrayDataSource";
    }
}
```

---

### 6. **Gestion des Logs d'Email**

```java
import java.sql.Timestamp;

/**
 * Service pour logger les emails envoyés
 */
public class EmailLogService {
    
    private Connection connection;
    
    public EmailLogService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }
    
    /**
     * Enregistre un email dans les logs
     */
    public void logEmail(String[] recipients, String subject, 
                       String status, String errorMessage) {
        
        String recipientsList = String.join(", ", recipients);
        
        String sql = "INSERT INTO email_log " +
                     "(recipient, subject, body, status, error_message) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, recipientsList);
            ps.setString(2, subject);
            ps.setString(3, "Contenu non stocké");  // Optionnel
            ps.setString(4, status);
            ps.setString(5, errorMessage);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère les logs d'emails pour un utilisateur
     */
    public List<EmailLog> getEmailsForUser(String email) {
        
        List<EmailLog> logs = new ArrayList<>();
        
        String sql = "SELECT * FROM email_log " +
                     "WHERE recipient LIKE ? " +
                     "ORDER BY sent_at DESC " +
                     "LIMIT 100";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + email + "%");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                EmailLog log = new EmailLog(
                    rs.getInt("id"),
                    rs.getString("recipient"),
                    rs.getString("subject"),
                    rs.getTimestamp("sent_at"),
                    rs.getString("status"),
                    rs.getString("error_message")
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
}
```

---

### 7. **Tâche Planifiée pour Rappels**

```java
import java.util.Timer;
import java.util.TimerTask;

/**
 * Gestionnaire de rappels automatiques
 */
public class ReminderScheduler {
    
    private Timer timer;
    private SeanceNotificationService notificationService;
    
    public ReminderScheduler() {
        this.timer = new Timer("ReminderScheduler", true);
        this.notificationService = new SeanceNotificationService();
    }
    
    /**
     * Démarre le scheduler de rappels
     */
    public void startScheduler() {
        
        // Vérifier toutes les heures
        long delay = 0;  // Immédiatement
        long period = 60 * 60 * 1000;  // Toutes les heures
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndSendReminders();
            }
        }, delay, period);
    }
    
    /**
     * Vérifie les séances à venir et envoie des rappels
     */
    private void checkAndSendReminders() {
        
        try {
            // Récupérer les séances des 24 prochaines heures
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp tomorrow = new Timestamp(
                System.currentTimeMillis() + (24 * 60 * 60 * 1000)
            );
            
            List<Seance> upcomingSeances = seanceService
                .getSeancesBetween(now, tomorrow);
            
            for (Seance seance : upcomingSeances) {
                
                // Calculer les heures restantes
                long hoursRemaining = calculateHoursRemaining(seance);
                
                // Envoyer rappel si 24h, 2h, 1h avant
                if (hoursRemaining == 24 || hoursRemaining == 2 || hoursRemaining == 1) {
                    
                    List<user> students = studentService
                        .getStudentsBySeance(seance.getId());
                    
                    notificationService.sendReminder(seance, students, (int)hoursRemaining);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Calcule les heures restantes avant une séance
     */
    private long calculateHoursRemaining(Seance seance) {
        
        long now = System.currentTimeMillis();
        long seanceTime = seance.getHeureDebut().getTime();
        
        long diffMs = seanceTime - now;
        long diffHours = diffMs / (1000 * 60 * 60);
        
        return diffHours;
    }
}
```

---

## 📊 Métriques et Statistiques

### 1. **Taux d'Envoi Réussi**

```java
/**
 * Calcule le taux de succès des emails envoyés
 */
public double calculateSuccessRate() {
    
    String sql = "SELECT COUNT(*) as total, " +
                 "SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success " +
                 "FROM email_log";
    
    try (Statement st = connection.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        
        if (rs.next()) {
            int total = rs.getInt("total");
            int success = rs.getInt("success");
            
            if (total == 0) return 0.0;
            
            return (double) success / total * 100;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return 0.0;
}
```

### 2. **Emails par Jour**

```java
/**
 * Compte les emails envoyés par jour
 */
public Map<LocalDate, Integer> getEmailsPerDay(int days) {
    
    Map<LocalDate, Integer> countByDay = new HashMap<>();
    
    LocalDate startDate = LocalDate.now().minusDays(days);
    
    String sql = "SELECT DATE(sent_at) as date, COUNT(*) as count " +
                 "FROM email_log " +
                 "WHERE sent_at >= ? " +
                 "GROUP BY DATE(sent_at) " +
                 "ORDER BY date DESC";
    
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setDate(1, java.sql.Date.valueOf(startDate));
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            LocalDate date = rs.getDate("date").toLocalDate();
            int count = rs.getInt("count");
            countByDay.put(date, count);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return countByDay;
}
```

---

## 🚀 Optimisations

### 1. **Queue d'Envoi Asynchrone**

```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Queue d'emails pour envoi asynchrone
 */
public class EmailQueue {
    
    private BlockingQueue<EmailTask> queue;
    private ExecutorService executorService;
    private MailingService mailingService;
    
    public EmailQueue(int poolSize) {
        this.queue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.mailingService = new MailingService();
        
        // Démarrer les workers
        for (int i = 0; i < poolSize; i++) {
            executorService.submit(this::worker);
        }
    }
    
    /**
     * Ajoute un email à la queue
     */
    public void enqueue(String[] to, String subject, String body) {
        queue.add(new EmailTask(to, subject, body));
    }
    
    /**
     * Worker qui traite la queue
     */
    private void worker() {
        while (true) {
            try {
                EmailTask task = queue.take();  // Bloquant
                mailingService.sendEmail(task.to, task.subject, task.body);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

/**
 * Tâche d'email
 */
class EmailTask {
    String[] to;
    String subject;
    String body;
    
    public EmailTask(String[] to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }
}
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **Protocole** | SMTP avec TLS/SSL |
| **Bibliothèque** | JavaMail API (javax.mail) |
| **Templates** | HTML avec placeholders |
| **Pièces Jointes** | MimeMultipart |
| **Logging** | Base de données |
| **Rappels** | TimerTask planifié |
| **Performance** | Queue asynchrone |
| **Sécurité** | Authentification + Chiffrement |

---

## 🔗 Ressources

- **JavaMail Documentation** : https://javaee.github.io/javamail/
- **SMTP RFC** : RFC 5321
- **Email HTML Guide** : Email on Acid

---

**Documentation générée pour EspritFlow - Système de Mailing** 📧
