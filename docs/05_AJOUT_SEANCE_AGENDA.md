# 📅 Documentation Fonctionnalité : Ajout de Séance à l'Agenda

## 📋 Vue d'ensemble

La fonctionnalité d'ajout de séance à l'agenda permet aux étudiants et enseignants d'ajouter automatiquement leurs séances à leur calendrier personnel (Google Calendar, Outlook, Apple Calendar, etc.) via des fichiers ICS (iCalendar) ou l'API Google Calendar.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **Bibliothèques Java**
- **iCal4j** : Génération de fichiers ICS/iCalendar
  - `net.fortuna.ical4j.model.Calendar` : Calendrier iCal
  - `net.fortuna.ical4j.model.VEvent` : Événement
  - `net.fortuna.ical4j.model.property.*` : Propriétés d'événement
- **Google Calendar API** : Synchronisation directe
  - `com.google.api.services.calendar.Calendar` : API Calendar
  - `com.google.api.services.calendar.model.Event` : Événement Google

### 2. **Formats**
- **ICS (iCalendar)** : Format standard RFC 5545
  - Extensible et universellement supporté
  - Compatible avec tous les calendriers
- **JSON** : Pour les réponses API Google

### 3. **Authentification**
- **OAuth 2.0** : Google Calendar
- **Client Credentials** : Accès API

---

## 🔧 Architecture du Système

### 1. **Schéma de Base de Données**

```sql
-- Ajouter une colonne pour stocker l'ID de l'événement Google
ALTER TABLE seance ADD COLUMN google_event_id TEXT;

-- Table pour les tokens OAuth des utilisateurs
CREATE TABLE google_oauth_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

---

### 2. **Flux de Traitement**

```
Bouton "Ajouter à l'Agenda"
    ↓
Service d'Export
    ↓
Génération fichier ICS OU Appel API Google
    ↓
Téléchargement/Intégration
    ↓
Sauvegarde google_event_id (si Google)
    ↓
Confirmation utilisateur
```

---

## 💻 Implémentation Technique

### 1. **Génération de Fichier ICS (Format Universel)**

```java
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.component.*;
import java.io.FileOutputStream;

/**
 * Service pour générer des fichiers ICS
 */
public class ICSService {
    
    /**
     * Génère un fichier ICS pour une séance
     * 
     * @param seance Séance à exporter
     * @param filePath Chemin de sauvegarde
     */
    public void generateICS(Seance seance, String filePath) throws Exception {
        
        // Créer le calendrier
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//EspritFlow//Calendar//FR"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        
        // Créer l'événement
        VEvent event = createVEventFromSeance(seance);
        
        // Ajouter l'événement au calendrier
        calendar.getComponents().add(event);
        
        // Sauvegarder en fichier ICS
        FileOutputStream fout = new FileOutputStream(filePath);
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, fout);
        fout.close();
    }
    
    /**
     * Crée un VEvent à partir d'une séance
     */
    private VEvent createVEventFromSeance(Seance seance) throws Exception {
        
        // Calculer les dates/heures de début et de fin
        java.util.Date startDate = new java.util.Date(seance.getHeureDebut().getTime());
        java.util.Date endDate = new java.util.Date(seance.getHeureFin().getTime());
        
        // Créer les propriétés de temps
        DtStart dtStart = new DtStart(startDate);
        dtStart.setUtc(true);  // UTC
        
        DtEnd dtEnd = new DtEnd(endDate);
        dtEnd.setUtc(true);
        
        // Créer l'événement
        String summary = buildEventSummary(seance);
        String description = buildEventDescription(seance);
        String location = getSalleName(seance.getSalleId());
        
        VEvent event = new VEvent(dtStart, dtEnd, summary);
        
        // Ajouter les propriétés
        event.getProperties().add(summary);
        event.getProperties().add(new Description(description));
        event.getProperties().add(new Location(location));
        
        // Rappels (15 min avant)
        VAlarm alarm = new VAlarm(new Dur(0, 0, -15));
        alarm.getProperties().add(Action.DISPLAY);
        alarm.getProperties().add(new Description("Rappel : " + summary));
        event.getAlarms().add(alarm);
        
        return event;
    }
    
    /**
     * Construit le résumé de l'événement
     */
    private String buildEventSummary(Seance seance) {
        
        String matiereName = getMatiereName(seance.getMatiereId());
        String type = seance.getTypeSeance();
        
        if ("Révision".equalsIgnoreCase(type)) {
            return "Séance de Révision : " + matiereName;
        }
        
        return matiereName + " (" + type + ")";
    }
    
    /**
     * Construit la description de l'événement
     */
    private String buildEventDescription(Seance seance) {
        
        StringBuilder desc = new StringBuilder();
        
        desc.append("Séance : ").append(getMatiereName(seance.getMatiereId())).append("\n");
        desc.append("Type : ").append(seance.getTypeSeance()).append("\n");
        desc.append("Jour : ").append(seance.getJour()).append("\n");
        desc.append("Horaires : ")
            .append(formatTime(seance.getHeureDebut()))
            .append(" - ")
            .append(formatTime(seance.getHeureFin()))
            .append("\n");
        desc.append("Salle : ").append(getSalleName(seance.getSalleId())).append("\n");
        desc.append("Classe : ").append(getClasseName(seance.getClasseId())).append("\n");
        desc.append("\nGénéré par EspritFlow");
        
        return desc.toString();
    }
}
```

---

### 2. **Contrôleur JavaFX pour l'Export**

```java
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

/**
 * Contrôleur pour l'export vers agenda
 */
public class AgendaExportController {
    
    @FXML private Button btnExportICS;
    @FXML private Button btnExportGoogle;
    
    private ICSService icsService;
    private GoogleCalendarService googleService;
    
    public void initialize() {
        icsService = new ICSService();
        googleService = new GoogleCalendarService();
    }
    
    /**
     * Exporte vers fichier ICS (compatible avec tous les calendriers)
     */
    @FXML
    private void handleExportICS() {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier ICS");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Calendar File (*.ics)", "*.ics")
        );
        
        // Nom par défaut
        String defaultName = "seance_" + seance.getId() + ".ics";
        fileChooser.setInitialFileName(defaultName);
        
        // Afficher le dialogue de sauvegarde
        File file = fileChooser.showSaveDialog(btnExportICS.getScene().getWindow());
        
        if (file != null) {
            try {
                icsService.generateICS(seance, file.getAbsolutePath());
                showSuccessAlert("Fichier ICS généré avec succès !");
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Erreur : " + e.getMessage());
            }
        }
    }
    
    /**
     * Exporte directement vers Google Calendar
     */
    @FXML
    private void handleExportGoogle() {
        
        try {
            // Vérifier si l'utilisateur a connecté son compte Google
            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (!googleService.isUserConnected(currentUser.getId())) {
                showAlert("Connexion Google requise", 
                         "Vous devez d'abord connecter votre compte Google Calendar.");
                return;
            }
            
            // Créer l'événement Google
            String eventId = googleService.createEvent(currentUser.getId(), seance);
            
            // Sauvegarder l'ID de l'événement
            seance.setGoogleEventId(eventId);
            seanceService.edit(seance);
            
            showSuccessAlert("Événement ajouté à Google Calendar !");
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur : " + e.getMessage());
        }
    }
}
```

---

### 3. **Intégration avec Google Calendar API**

```java
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

/**
 * Service Google Calendar
 */
public class GoogleCalendarService {
    
    private static final String APPLICATION_NAME = "EspritFlow";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    
    private static final List<String> SCOPES = 
        Collections.singletonList(CalendarScopes.CALENDAR);
    
    /**
     * Crée un événement dans Google Calendar
     * 
     * @param userId ID de l'utilisateur
     * @param seance Séance à ajouter
     * @return ID de l'événement créé
     */
    public String createEvent(int userId, Seance seance) throws Exception {
        
        // 1. Obtenir les credentials OAuth
        Credential credential = getCredentials(userId);
        
        // 2. Créer le service Calendar
        Calendar service = new Calendar.Builder(
            NetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            credential
        ).setApplicationName(APPLICATION_NAME)
         .build();
        
        // 3. Créer l'événement
        Event event = new Event()
            .setSummary(buildSummary(seance))
            .setLocation(getSalleName(seance.getSalleId()))
            .setDescription(buildDescription(seance));
        
        // Dates de début et de fin
        java.util.Date startDate = new java.util.Date(seance.getHeureDebut().getTime());
        java.util.Date endDate = new java.util.Date(seance.getHeureFin().getTime());
        
        DateTime startDateTime = new DateTime(startDate);
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Africa/Tunis");
        event.setStart(start);
        
        DateTime endDateTime = new DateTime(endDate);
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Africa/Tunis");
        event.setEnd(end);
        
        // Rappels
        EventReminder[] reminders = new EventReminder[]{
            new EventReminder().setMethod("email").setMinutes(24 * 60),  // 1 jour avant
            new EventReminder().setMethod("popup").setMinutes(15)       // 15 min avant
        };
        event.setReminders(new Event.Reminders().setUseDefault(false)
                                           .setOverrides(Arrays.asList(reminders)));
        
        // 4. Insérer l'événement
        String calendarId = "primary";  // Calendrier principal de l'utilisateur
        Event createdEvent = service.events().insert(calendarId, event).execute();
        
        // 5. Retourner l'ID de l'événement
        return createdEvent.getId();
    }
    
    /**
     * Met à jour un événement existant dans Google Calendar
     */
    public void updateEvent(int userId, Seance seance) throws Exception {
        
        Credential credential = getCredentials(userId);
        Calendar service = new Calendar.Builder(
            NetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            credential
        ).setApplicationName(APPLICATION_NAME).build();
        
        if (seance.getGoogleEventId() == null) {
            throw new Exception("Aucun Google Event ID associé à cette séance");
        }
        
        // Récupérer l'événement existant
        Event event = service.events().get("primary", seance.getGoogleEventId()).execute();
        
        // Mettre à jour les champs
        event.setSummary(buildSummary(seance));
        event.setLocation(getSalleName(seance.getSalleId()));
        event.setDescription(buildDescription(seance));
        
        java.util.Date startDate = new java.util.Date(seance.getHeureDebut().getTime());
        java.util.Date endDate = new java.util.Date(seance.getHeureFin().getTime());
        
        event.setStart(new EventDateTime()
            .setDateTime(new DateTime(startDate))
            .setTimeZone("Africa/Tunis"));
        event.setEnd(new EventDateTime()
            .setDateTime(new DateTime(endDate))
            .setTimeZone("Africa/Tunis"));
        
        // Mettre à jour
        service.events().update("primary", event.getId(), event).execute();
    }
    
    /**
     * Supprime un événement de Google Calendar
     */
    public void deleteEvent(int userId, String eventId) throws Exception {
        
        Credential credential = getCredentials(userId);
        Calendar service = new Calendar.Builder(
            NetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            credential
        ).setApplicationName(APPLICATION_NAME).build();
        
        service.events().delete("primary", eventId).execute();
    }
    
    /**
     * Obtient les credentials OAuth pour un utilisateur
     */
    private Credential getCredentials(int userId) throws Exception {
        
        // Récupérer les tokens depuis la base de données
        GoogleOAuthToken token = getOAuthToken(userId);
        
        // Créer le credential
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(NetHttpTransport.newTrustedTransport())
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(getClientSecrets())
            .build();
        
        credential.setAccessToken(token.getAccessToken());
        credential.setRefreshToken(token.getRefreshToken());
        credential.setExpiresInSeconds(
            (token.getExpiresAt().getTime() - System.currentTimeMillis()) / 1000
        );
        
        return credential;
    }
    
    /**
     * Vérifie si l'utilisateur a connecté son compte Google
     */
    public boolean isUserConnected(int userId) {
        
        try {
            GoogleOAuthToken token = getOAuthToken(userId);
            return token != null && 
                   token.getExpiresAt().after(new java.util.Date());
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

### 4. **OAuth 2.0 Flow pour Google Calendar**

```java
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

/**
 * Gestionnaire OAuth 2.0 pour Google
 */
public class GoogleAuthManager {
    
    private static final String CLIENT_ID = "votre_client_id.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "votre_client_secret";
    
    /**
     * Obtient un nouveau access token via le flow OAuth 2.0
     * 
     * @param userId ID de l'utilisateur
     * @return Credential OAuth
     */
    public Credential authorize(int userId) throws Exception {
        
        // Chargement des secrets client
        GoogleClientSecrets clientSecrets = loadClientSecrets();
        
        // Créer le flow d'autorisation
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            NetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            clientSecrets,
            SCOPES
        )
        .setDataStoreFactory(new FileDataStoreFactory(
            new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")  // Pour refresh token
        .build();
        
        // Lancer le flow d'autorisation
        Credential credential = new AuthorizationCodeInstalledApp(
            flow,
            new LocalServerReceiver.Builder()
                .setPort(8888)
                .build()
        ).authorize("user");
        
        // Sauvegarder les tokens dans la base de données
        saveOAuthToken(userId, credential);
        
        return credential;
    }
    
    /**
     * Rafraîchit le token d'accès
     */
    public Credential refreshAccessToken(int userId) throws Exception {
        
        GoogleOAuthToken token = getOAuthToken(userId);
        
        // Créer le credential avec le refresh token
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(NetHttpTransport.newTrustedTransport())
            .setJsonFactory(JacksonFactory.getDefaultInstance())
            .setClientSecrets(loadClientSecrets())
            .build();
        
        credential.setRefreshToken(token.getRefreshToken());
        
        // Rafraîchir
        credential.refreshToken();
        
        // Sauvegarder les nouveaux tokens
        saveOAuthToken(userId, credential);
        
        return credential;
    }
    
    /**
     * Sauvegarde les tokens OAuth dans la base de données
     */
    private void saveOAuthToken(int userId, Credential credential) throws SQLException {
        
        String sql = "INSERT INTO google_oauth_tokens " +
                     "(user_id, access_token, refresh_token, expires_at) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "access_token = VALUES(access_token), " +
                     "refresh_token = VALUES(refresh_token), " +
                     "expires_at = VALUES(expires_at)";
        
        long expiresAt = System.currentTimeMillis() + 
                         (credential.getExpiresInSeconds() * 1000);
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, credential.getAccessToken());
            ps.setString(3, credential.getRefreshToken());
            ps.setTimestamp(4, new Timestamp(expiresAt));
            ps.executeUpdate();
        }
    }
}
```

---

### 5. **Export en Masse**

```java
/**
 * Exporte toutes les séances d'une semaine vers un seul fichier ICS
 */
public void exportWeekToICS(int classeId, LocalDate weekStart) throws Exception {
    
    // Créer le calendrier
    Calendar calendar = new Calendar();
    calendar.getProperties().add(new ProdId("-//EspritFlow//Calendar//FR"));
    calendar.getProperties().add(Version.VERSION_2_0);
    
    // Récupérer les séances de la semaine
    LocalDate weekEnd = weekStart.plusDays(6);
    List<Seance> weekSeances = seanceService.getSeancesBetween(
        Timestamp.valueOf(weekStart.atStartOfDay()),
        Timestamp.valueOf(weekEnd.atTime(23, 59))
    );
    
    // Créer un événement pour chaque séance
    for (Seance seance : weekSeances) {
        VEvent event = createVEventFromSeance(seance);
        calendar.getComponents().add(event);
    }
    
    // Sauvegarder
    String fileName = "semaine_" + weekStart.format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + ".ics";
    String filePath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;
    
    FileOutputStream fout = new FileOutputStream(filePath);
    CalendarOutputter outputter = new CalendarOutputter();
    outputter.output(calendar, fout);
    fout.close();
    
    showSuccessAlert("Calendrier de la semaine exporté !");
}
```

---

## 📊 Synchronisation Bidirectionnelle

### 1. **Import depuis Google Calendar**

```java
/**
 * Synchronise les séances de Google Calendar vers EspritFlow
 */
public void syncFromGoogle(int userId) throws Exception {
    
    Credential credential = googleService.getCredentials(userId);
    Calendar service = new Calendar.Builder(
        NetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        credential
    ).setApplicationName(APPLICATION_NAME).build();
    
    // Récupérer les événements des 30 prochains jours
    DateTime now = new DateTime(System.currentTimeMillis());
    Events events = service.events().list("primary")
        .setMaxResults(250)
        .setTimeMin(now)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute();
    
    // Traiter chaque événement
    for (Event event : events.getItems()) {
        
        // Vérifier si l'événement est déjà importé
        if (isAlreadyImported(event.getId())) {
            continue;
        }
        
        // Créer une séance EspritFlow
        Seance seance = createSeanceFromEvent(event, userId);
        
        // Sauvegarder et lier l'ID Google
        seance.setGoogleEventId(event.getId());
        seanceService.add(seance);
    }
}
```

---

## 🚀 Optimisations

### 1. **Mise en Cache des Credentials**

```java
private Map<Integer, Credential> credentialsCache = new HashMap<>();

/**
 * Obtient un credential depuis le cache ou le rafraîchit
 */
private Credential getCachedCredential(int userId) throws Exception {
    
    // Vérifier le cache
    if (credentialsCache.containsKey(userId)) {
        Credential cached = credentialsCache.get(userId);
        
        // Vérifier l'expiration
        if (cached.getExpiresInSeconds() > 60) {  // Plus d'1 minute
            return cached;
        }
    }
    
    // Rafraîchir le token
    Credential fresh = refreshAccessToken(userId);
    
    // Mettre en cache
    credentialsCache.put(userId, fresh);
    
    return fresh;
}
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **Format Universel** | ICS (iCalendar) RFC 5545 |
| **Bibliothèque ICS** | iCal4j |
| **API Google** | Google Calendar API v3 |
| **Authentification** | OAuth 2.0 |
| **Tokens** | Access + Refresh tokens |
| **Synchronisation** | Bidirectionnelle |
| **Rappels** | Email + Popup (Google) |
| **Export** | Individuel ou en masse |

---

## 🔗 Ressources

- **iCal4j** : https://github.com/ical4j/ical4j
- **Google Calendar API** : https://developers.google.com/calendar
- **RFC 5545** : iCalendar Specification

---

**Documentation générée pour EspritFlow - Ajout de Séance à l'Agenda** 📅
