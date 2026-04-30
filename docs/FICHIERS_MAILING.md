# 📂 Fichiers Système de Mailing - Localisation Complète

## 🎯 Vue d'ensemble
Cette fonctionnalité permet d'envoyer des emails aux étudiants pour notifier des changements de séances, rappels de cours, et autres communications importantes.

---

## 📁 Structure des Fichiers

### 1️⃣ Contrôleurs (Controllers)

#### SeanceContentController.java (Backoffice)
**Emplacement** : `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java`

**Responsabilité** : Envoi d'emails aux étudiants pour les modifications de séances

**Méthodes clés** :

```java
// Ligne 550-600 : Envoi d'emails pour une séance
@FXML
private void sendEmailsForSeance() {
    Seance selectedSeance = getSelectedSeance();
    if (selectedSeance == null) {
        showAlert("Erreur", "Veuillez sélectionner une séance");
        return;
    }
    
    try {
        // Récupérer les étudiants de la classe
        List<user> students = userServices.getStudentsByClass(selectedSeance.getClasseId());
        
        if (students.isEmpty()) {
            showAlert("Information", "Aucun étudiant dans cette classe");
            return;
        }
        
        // Récupérer les informations de la séance
        Matiere matiere = matiereService.getById(selectedSeance.getMatiereId());
        Salle salle = salleService.getById(selectedSeance.getSalleId());
        Classe classe = classeService.getById(selectedSeance.getClasseId());
        
        // Préparer le contenu de l'email
        String subject = "Modification de séance : " + matiere.getNom();
        String body = prepareEmailBody(selectedSeance, matiere, salle, classe);
        
        // Envoyer l'email à tous les étudiants
        int sentCount = 0;
        for (user student : students) {
            if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                boolean sent = emailService.sendEmail(
                    student.getEmail(), 
                    subject, 
                    body
                );
                if (sent) {
                    sentCount++;
                }
            }
        }
        
        showAlert("Succès", sentCount + " emails envoyés avec succès !");
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Impossible d'envoyer les emails");
    }
}

// Ligne 605-660 : Préparation du corps de l'email
private String prepareEmailBody(Seance seance, Matiere matiere, Salle salle, Classe classe) {
    StringBuilder body = new StringBuilder();
    
    body.append("<html><body style='font-family: Arial, sans-serif;'>");
    body.append("<div style='max-width: 600px; margin: 0 auto;'>");
    
    // En-tête
    body.append("<div style='background-color: #2563eb; color: white; padding: 20px; text-align: center;'>");
    body.append("<h1>📚 Notification de Séance</h1>");
    body.append("</div>");
    
    // Contenu
    body.append("<div style='padding: 20px; background-color: #f8f9fa;'>");
    body.append("<p>Cher(e) étudiant(e),</p>");
    body.append("<p>Nous vous informons d'une modification dans votre emploi du temps :</p>");
    
    // Détails de la séance
    body.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
    body.append("<tr><td style='padding: 10px; background-color: #e9ecef; font-weight: bold;'>Matière</td><td style='padding: 10px;'>" + matiere.getNom() + "</td></tr>");
    body.append("<tr><td style='padding: 10px; background-color: #e9ecef; font-weight: bold;'>Jour</td><td style='padding: 10px;'>" + seance.getJour() + "</td></tr>");
    body.append("<tr><td style='padding: 10px; background-color: #e9ecef; font-weight: bold;'>Heure</td><td style='padding: 10px;'>" + 
                seance.getHeureDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                " - " + 
                seance.getHeureFin().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                "</td></tr>");
    body.append("<tr><td style='padding: 10px; background-color: #e9ecef; font-weight: bold;'>Salle</td><td style='padding: 10px;'>" + 
                salle.getBlock() + salle.getNumber() + "</td></tr>");
    body.append("<tr><td style='padding: 10px; background-color: #e9ecef; font-weight: bold;'>Classe</td><td style='padding: 10px;'>" + 
                classe.getNom() + "</td></tr>");
    body.append("</table>");
    
    // Footer
    body.append("<p style='color: #666; font-size: 12px;'>Cet email a été envoyé automatiquement par EspritFlow.</p>");
    body.append("</div>");
    body.append("</div>");
    body.append("</body></html>");
    
    return body.toString();
}
```

**Où trouver les données** :
- Étudiants : `src/main/java/piJava/services/UserServices.java`
- Séances : `src/main/java/piJava/services/SeanceService.java`
- Matières : `src/main/java/piJava/services/MatiereService.java`
- Salles : `src/main/java/piJava/services/SalleService.java`
- Classes : `src/main/java/piJava/services/ClasseService.java`

---

#### EmploiContentController.java (Frontoffice)
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`

**Responsabilité** : Envoi d'emails de notification pour l'emploi du temps

**Méthodes clés** :

```java
// Ligne 320-370 : Envoi d'email de notification
@FXML
private void sendEmailNotification() {
    try {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            showAlert("Erreur", "Aucun email configuré pour votre compte");
            return;
        }
        
        // Générer le PDF de l'emploi du temps
        List<Seance> seances = seanceService.getAllSeances();
        List<Seance> userSeances = seances.stream()
            .filter(s -> s.getClasseId().equals(currentUser.getClasse_id()))
            .collect(Collectors.toList());
        
        String pdfFileName = "Emploi_du_temps_temp.pdf";
        generatePDF(userSeances, currentUser, pdfFileName);
        
        // Envoyer l'email avec pièce jointe
        String subject = "Votre emploi du temps - EspritFlow";
        String body = prepareScheduleEmailBody(currentUser);
        
        boolean sent = emailService.sendEmailWithAttachment(
            currentUser.getEmail(),
            subject,
            body,
            pdfFileName
        );
        
        if (sent) {
            showAlert("Succès", "Emploi du temps envoyé par email !");
        } else {
            showAlert("Erreur", "Impossible d'envoyer l'email");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Erreur lors de l'envoi de l'email");
    }
}

// Ligne 375-420 : Préparation du corps de l'email emploi du temps
private String prepareScheduleEmailBody(user currentUser) {
    StringBuilder body = new StringBuilder();
    
    body.append("<html><body style='font-family: Arial, sans-serif;'>");
    body.append("<div style='max-width: 600px; margin: 0 auto;'>");
    
    body.append("<div style='background-color: #10b981; color: white; padding: 20px; text-align: center;'>");
    body.append("<h1>📅 Votre Emploi du Temps</h1>");
    body.append("</div>");
    
    body.append("<div style='padding: 20px; background-color: #f8f9fa;'>");
    body.append("<p>Bonjour " + currentUser.getPrenom() + ",</p>");
    body.append("<p>Veuillez trouver ci-joint votre emploi du temps à jour.</p>");
    body.append("<p>Cordialement,</p>");
    body.append("<p>L'équipe EspritFlow</p>");
    body.append("</div>");
    
    body.append("</div>");
    body.append("</body></html>");
    
    return body.toString();
}
```

---

### 2️⃣ Services (Business Logic)

#### EmailService.java
**Emplacement** : `src/main/java/piJava/services/EmailService.java`

**Responsabilité** : Gestion de l'envoi d'emails via SMTP

**Méthodes clés** :

```java
// Ligne 20-40 : Configuration SMTP
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String SMTP_USERNAME = "espritflow.esprit@gmail.com"; // À configurer
private static final String SMTP_PASSWORD = "votre_mot_de_passe";           // À configurer

// Ligne 45-85 : Envoi d'un email simple
public boolean sendEmail(String to, String subject, String body) {
    Properties props = new Properties();
    props.put("mail.smtp.host", SMTP_HOST);
    props.put("mail.smtp.port", SMTP_PORT);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    
    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
        }
    });
    
    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        
        // Contenu HTML
        message.setContent(body, "text/html; charset=utf-8");
        
        Transport.send(message);
        return true;
    } catch (MessagingException e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 90-150 : Envoi d'un email avec pièce jointe
public boolean sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
    Properties props = new Properties();
    props.put("mail.smtp.host", SMTP_HOST);
    props.put("mail.smtp.port", SMTP_PORT);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    
    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
        }
    });
    
    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        
        // Création du multipart
        Multipart multipart = new MimeMultipart();
        
        // Partie texte/HTML
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html; charset=utf-8");
        multipart.addBodyPart(messageBodyPart);
        
        // Partie pièce jointe
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(new File(attachmentPath));
        multipart.addBodyPart(attachmentPart);
        
        message.setContent(multipart);
        Transport.send(message);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 155-200 : Envoi d'emails en masse
public int sendBulkEmail(List<String> recipients, String subject, String body) {
    int successCount = 0;
    
    Properties props = new Properties();
    props.put("mail.smtp.host", SMTP_HOST);
    props.put("mail.smtp.port", SMTP_PORT);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    
    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
        }
    });
    
    for (String recipient : recipients) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
            successCount++;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    
    return successCount;
}
```

**Où trouver les données** :
- Configuration SMTP : Définie dans les constantes du service
- Templates HTML : Générés dynamiquement dans les contrôleurs
- Pièces jointes : Fichiers temporaires générés (PDF, ICS, etc.)

---

#### UserServices.java
**Emplacement** : `src/main/java/piJava/services/UserServices.java`

**Méthodes utilisées pour le mailing** :

```java
// Ligne 200-230 : Récupérer les étudiants d'une classe
public List<user> getStudentsByClass(int classeId) {
    List<user> students = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM user WHERE classe_id = ? AND role = 'STUDENT'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, classeId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            user u = new user();
            // Mapping des champs
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            students.add(u);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return students;
}
```

**Où trouver les données** :
- Table BDD : `user`
- Colonnes utilisées : `id`, `nom`, `prenom`, `email`, `classe_id`, `role`

---

### 3️⃣ Fichiers FXML (UI Views)

#### SeanceContent.fxml
**Emplacement** : `src/main/resources/backoffice/Seance/SeanceContent.fxml`

**Bouton pour envoyer des emails** :

```xml
<!-- Ligne 45-50 : Bouton Envoyer Emails -->
<Button text="Envoyer Emails" onAction="#sendEmailsForSeance" styleClass="email-button"/>
```

---

#### EmploiContent.fxml
**Emplacement** : `src/main/resources/frontoffice/emploi/EmploiContent.fxml`

**Bouton pour envoyer l'emploi du temps par email** :

```xml
<!-- Ligne 25-30 : Bouton Email -->
<Button fx:id="sendEmailButton" text="Envoyer par Email" onAction="#sendEmailNotification" styleClass="email-button"/>
```

---

### 4️⃣ Styles CSS

#### seance.css
**Emplacement** : `src/main/resources/backoffice/Seance/seance.css`

**Styles pour le bouton d'email** :

```css
/* Ligne 20-30 */
.email-button {
    -fx-background-color: #2563eb;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-cursor: hand;
    -fx-padding: 10 20;
}

.email-button:hover {
    -fx-background-color: #1d4ed8;
}
```

---

## 📊 Flux de Données

### Envoi d'emails pour une séance (Backoffice)
```
[Admin clique sur "Envoyer Emails"]
        ↓
[SeanceContentController.sendEmailsForSeance()]
        ↓
[UserServices.getStudentsByClass()]
        ↓
[MatiereService.getById()] → Nom matière
        ↓
[SalleService.getById()] → Nom salle
        ↓
[ClasseService.getById()] → Nom classe
        ↓
[prepareEmailBody()] → Template HTML
        ↓
[EmailService.sendBulkEmail()]
        ↓
[SMTP Server] → Envoi aux étudiants
```

### Envoi d'emploi du temps par email (Frontoffice)
```
[Étudiant clique sur "Envoyer par Email"]
        ↓
[EmploiContentController.sendEmailNotification()]
        ↓
[Génération PDF temporaire]
        ↓
[prepareScheduleEmailBody()] → Template HTML
        ↓
[EmailService.sendEmailWithAttachment()]
        ↓
[SMTP Server] → Envoi avec PDF
```

---

## 🔍 Comment Trouver les Données

### Pour modifier la configuration SMTP :
1. **Fichier** : `src/main/java/piJava/services/EmailService.java`
2. **Lignes** : 20-40
3. **Variables à modifier** :
   - `SMTP_HOST` : Serveur SMTP
   - `SMTP_PORT` : Port SMTP
   - `SMTP_USERNAME` : Nom d'utilisateur
   - `SMTP_PASSWORD` : Mot de passe

### Pour modifier le template d'email de séance :
1. **Fichier** : `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java`
2. **Méthode** : `prepareEmailBody()` (lignes 605-660)
3. **Éléments modifiables** : HTML, CSS inline, contenu du message

### Pour modifier le template d'email d'emploi du temps :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`
2. **Méthode** : `prepareScheduleEmailBody()` (lignes 375-420)
3. **Éléments modifiables** : HTML, CSS inline, contenu du message

### Pour ajouter un nouveau type d'email :
1. **Créer** une nouvelle méthode dans le contrôleur approprié
2. **Générer** le contenu HTML avec `StringBuilder`
3. **Appeler** `emailService.sendEmail()` ou `sendEmailWithAttachment()`

---

## 📧 Format des Emails

### Structure HTML d'email
```html
<html>
<body style='font-family: Arial, sans-serif;'>
    <div style='max-width: 600px; margin: 0 auto;'>
        <!-- Header coloré -->
        <div style='background-color: #2563eb; color: white; padding: 20px; text-align: center;'>
            <h1>Titre</h1>
        </div>
        
        <!-- Contenu -->
        <div style='padding: 20px; background-color: #f8f9fa;'>
            <p>Message...</p>
        </div>
        
        <!-- Footer -->
        <p style='color: #666; font-size: 12px;'>Signature</p>
    </div>
</body>
</html>
```

### Configuration SMTP (Gmail)
| Paramètre | Valeur |
|-----------|--------|
| Host | smtp.gmail.com |
| Port | 587 (TLS) |
| Authentification | Oui |
| STARTTLS | Oui |

---

## 🚀 Points d'Entrée

### Backoffice
- **Menu Séances** → Sélectionner une séance → Bouton "Envoyer Emails" → Envoi aux étudiants de la classe

### Frontoffice
- **Menu Emploi** → Bouton "Envoyer par Email" → Envoi de l'emploi du temps en PDF

---

**Dernière mise à jour** : 30 avril 2026
