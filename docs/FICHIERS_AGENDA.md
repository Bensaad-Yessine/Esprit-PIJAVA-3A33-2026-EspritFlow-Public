# 📂 Fichiers Ajout Séance à l'Agenda - Localisation Complète

## 🎯 Vue d'ensemble
Cette fonctionnalité permet d'ajouter des séances à l'agenda Google Calendar ou de les exporter au format ICS pour les intégrer dans n'importe quel calendrier.

---

## 📁 Structure des Fichiers

### 1️⃣ Contrôleurs (Controllers)

#### EmploiContentController.java (Frontoffice)
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`

**Responsabilité** : Ajout de séances à l'agenda depuis l'emploi du temps

**Méthodes clés** :

```java
// Ligne 50-70 : Déclarations des composants
@FXML
private VBox scheduleContainer;
@FXML
private Button addToAgendaButton;
@FXML
private Button exportICSButton;

// Services
private SeanceService seanceService;
private GoogleCalendarService googleCalendarService;

// Ligne 75-100 : Initialisation
@Override
public void initialize(URL location, ResourceBundle resources) {
    seanceService = new SeanceService();
    googleCalendarService = new GoogleCalendarService();
    loadEmploiDuTemps();
}

// Ligne 105-150 : Ajout de toutes les séances à Google Calendar
@FXML
private void addToGoogleCalendar() {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    
    if (!googleCalendarService.isAuthenticated()) {
        showAlert("Information", "Vous devez vous connecter à Google Calendar");
        boolean connected = googleCalendarService.authenticate();
        if (!connected) {
            return;
        }
    }
    
    try {
        List<Seance> seances = seanceService.getAllSeances();
        List<Seance> userSeances = seances.stream()
            .filter(s -> s.getClasseId().equals(currentUser.getClasse_id()))
            .collect(Collectors.toList());
        
        int addedCount = 0;
        for (Seance seance : userSeances) {
            boolean added = googleCalendarService.addSeanceToCalendar(seance, currentUser);
            if (added) {
                addedCount++;
            }
        }
        
        showAlert("Succès", addedCount + " séances ajoutées à Google Calendar !");
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Impossible d'ajouter les séances à Google Calendar");
    }
}

// Ligne 155-190 : Ajout d'une séance spécifique à Google Calendar
@FXML
private void addSelectedSeanceToCalendar() {
    Seance selectedSeance = getSelectedSeance();
    if (selectedSeance == null) {
        showAlert("Erreur", "Veuillez sélectionner une séance");
        return;
    }
    
    try {
        if (!googleCalendarService.isAuthenticated()) {
            showAlert("Information", "Vous devez vous connecter à Google Calendar");
            boolean connected = googleCalendarService.authenticate();
            if (!connected) {
                return;
            }
        }
        
        user currentUser = SessionManager.getInstance().getCurrentUser();
        boolean added = googleCalendarService.addSeanceToCalendar(selectedSeance, currentUser);
        
        if (added) {
            showAlert("Succès", "Séance ajoutée à Google Calendar !");
        } else {
            showAlert("Erreur", "Impossible d'ajouter la séance");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Erreur lors de l'ajout de la séance");
    }
}

// Ligne 195-240 : Export au format ICS
@FXML
private void exportToICS() {
    try {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        List<Seance> seances = seanceService.getAllSeances();
        List<Seance> userSeances = seances.stream()
            .filter(s -> s.getClasseId().equals(currentUser.getClasse_id()))
            .collect(Collectors.toList());
        
        String icsFileName = "Emploi_du_temps_" + currentUser.getNom() + "_" + 
                             currentUser.getPrenom() + ".ics";
        
        boolean exported = googleCalendarService.exportToICS(userSeances, icsFileName);
        
        if (exported) {
            showAlert("Succès", "Fichier ICS exporté : " + icsFileName);
        } else {
            showAlert("Erreur", "Impossible d'exporter le fichier ICS");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Erreur lors de l'export ICS");
    }
}
```

**Où trouver les données** :
- Séances : `src/main/java/piJava/services/SeanceService.java`
- Google Calendar : `src/main/java/piJava/services/GoogleCalendarService.java`
- Utilisateur : `SessionManager.getInstance().getCurrentUser()`

---

#### SeanceContentController.java (Backoffice)
**Emplacement** : `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java`

**Responsabilité** : Ajout d'une séance spécifique à l'agenda

**Méthodes clés** :

```java
// Ligne 650-690 : Ajout d'une séance à l'agenda
@FXML
private void addSeanceToAgenda() {
    Seance selectedSeance = getSelectedSeance();
    if (selectedSeance == null) {
        showAlert("Erreur", "Veuillez sélectionner une séance");
        return;
    }
    
    try {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        boolean added = googleCalendarService.addSeanceToCalendar(selectedSeance, currentUser);
        
        if (added) {
            showAlert("Succès", "Séance ajoutée à Google Calendar !");
        } else {
            showAlert("Erreur", "Impossible d'ajouter la séance");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Erreur lors de l'ajout de la séance");
    }
}

// Ligne 695-730 : Export au format ICS pour une séance
@FXML
private void exportSeanceToICS() {
    Seance selectedSeance = getSelectedSeance();
    if (selectedSeance == null) {
        showAlert("Erreur", "Veuillez sélectionner une séance");
        return;
    }
    
    try {
        String icsFileName = "Seance_" + selectedSeance.getId() + ".ics";
        
        List<Seance> seances = new ArrayList<>();
        seances.add(selectedSeance);
        
        boolean exported = googleCalendarService.exportToICS(seances, icsFileName);
        
        if (exported) {
            showAlert("Succès", "Fichier ICS exporté : " + icsFileName);
        } else {
            showAlert("Erreur", "Impossible d'exporter le fichier ICS");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Erreur lors de l'export ICS");
    }
}
```

---

### 2️⃣ Services (Business Logic)

#### GoogleCalendarService.java
**Emplacement** : `src/main/java/piJava/services/GoogleCalendarService.java`

**Responsabilité** : Intégration avec Google Calendar API et export ICS

**Méthodes clés** :

```java
// Ligne 20-40 : Configuration Google Calendar API
private static final String APPLICATION_NAME = "EspritFlow";
private static final String CREDENTIALS_FILE_PATH = "credentials.json";
private static final String TOKENS_DIRECTORY_PATH = "tokens";
private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

private Calendar service = null;

// Ligne 45-90 : Authentification OAuth2
public boolean authenticate() {
    try {
        // Chargement des credentials
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            JacksonFactory.getDefaultInstance(),
            new InputStreamReader(in)
        );
        
        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            JacksonFactory.getDefaultInstance(),
            NetHttpTransport.newTrustedTransport(),
            clientSecrets,
            SCOPES
        )
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        
        // Build service
        service = new Calendar.Builder(
            NetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName(APPLICATION_NAME)
        .build();
        
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 95-140 : Vérification de l'authentification
public boolean isAuthenticated() {
    return service != null;
}

// Ligne 145-200 : Ajout d'une séance à Google Calendar
public boolean addSeanceToCalendar(Seance seance, user currentUser) {
    try {
        // Récupérer les détails de la séance
        MatiereService matiereService = new MatiereService();
        SalleService salleService = new SalleService();
        
        Matiere matiere = matiereService.getById(seance.getMatiereId());
        Salle salle = salleService.getById(seance.getSalleId());
        
        // Créer l'événement
        Event event = new Event()
            .setSummary(matiere.getNom() + " - " + seance.getTypeSeance())
            .setDescription("Classe : " + currentUser.getClasse_id() + 
                          "\\nSalle : " + salle.getBlock() + salle.getNumber() + 
                          "\\nMode : " + seance.getMode())
            .setLocation("ESPRIT - " + salle.getBlock() + salle.getNumber());
        
        // Définir les dates
        DateTime startDateTime = new DateTime(seance.getHeureDebut().getTime());
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Africa/Tunis");
        event.setStart(start);
        
        DateTime endDateTime = new DateTime(seance.getHeureFin().getTime());
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Africa/Tunis");
        event.setEnd(end);
        
        // Ajouter des rappels
        Event.Reminder[] reminders = new Event.Reminder[]{
            new Event.Reminder().setMethod("email").setMinutes(24 * 60),
            new Event.Reminder().setMethod("popup").setMinutes(10)
        };
        event.setReminders(new Event.Reminders().setUseDefault(false).setOverrides(Arrays.asList(reminders)));
        
        // Insérer l'événement
        event = service.events().insert("primary", event).execute();
        System.out.println("Event created: " + event.getHtmlLink());
        
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 205-280 : Export au format ICS
public boolean exportToICS(List<Seance> seances, String fileName) {
    try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        
        // En-tête du fichier ICS
        writer.write("BEGIN:VCALENDAR");
        writer.newLine();
        writer.write("VERSION:2.0");
        writer.newLine();
        writer.write("PRODID:-//EspritFlow//EspritFlow Calendar//EN");
        writer.newLine();
        writer.write("CALSCALE:GREGORIAN");
        writer.newLine();
        writer.write("METHOD:PUBLISH");
        writer.newLine();
        
        // Récupérer les services
        MatiereService matiereService = new MatiereService();
        SalleService salleService = new SalleService();
        
        // Ajouter chaque séance comme événement
        for (Seance seance : seances) {
            Matiere matiere = matiereService.getById(seance.getMatiereId());
            Salle salle = salleService.getById(seance.getSalleId());
            
            writer.write("BEGIN:VEVENT");
            writer.newLine();
            
            // UID unique
            writer.write("UID:" + UUID.randomUUID().toString() + "@espritflow.tn");
            writer.newLine();
            
            // Titre
            writer.write("SUMMARY:" + matiere.getNom() + " - " + seance.getTypeSeance());
            writer.newLine();
            
            // Description
            writer.write("DESCRIPTION:Classe : " + seance.getClasseId() + 
                        "\\nSalle : " + salle.getBlock() + salle.getNumber() + 
                        "\\nMode : " + seance.getMode());
            writer.newLine();
            
            // Lieu
            writer.write("LOCATION:ESPRIT - " + salle.getBlock() + salle.getNumber());
            writer.newLine();
            
            // Heure de début
            LocalDateTime start = seance.getHeureDebut().toLocalDateTime();
            writer.write("DTSTART:" + start.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")));
            writer.newLine();
            
            // Heure de fin
            LocalDateTime end = seance.getHeureFin().toLocalDateTime();
            writer.write("DTEND:" + end.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")));
            writer.newLine();
            
            // Rappels
            writer.write("BEGIN:VALARM");
            writer.newLine();
            writer.write("TRIGGER:-PT15M");
            writer.newLine();
            writer.write("ACTION:DISPLAY");
            writer.newLine();
            writer.write("DESCRIPTION:Reminder");
            writer.newLine();
            writer.write("END:VALARM");
            writer.newLine();
            
            writer.write("END:VEVENT");
            writer.newLine();
        }
        
        // Pied du fichier ICS
        writer.write("END:VCALENDAR");
        writer.newLine();
        writer.close();
        
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 285-320 : Suppression d'un événement du calendrier
public boolean deleteFromCalendar(String eventId) {
    try {
        if (service == null) {
            return false;
        }
        
        service.events().delete("primary", eventId).execute();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 325-370 : Récupération des événements du calendrier
public List<Event> getCalendarEvents(LocalDate startDate, LocalDate endDate) {
    List<Event> events = new ArrayList<>();
    
    try {
        if (service == null) {
            return events;
        }
        
        DateTime startDateTime = new DateTime(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        DateTime endDateTime = new DateTime(endDate.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        
        Events result = service.events().list("primary")
            .setTimeMin(startDateTime)
            .setTimeMax(endDateTime)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
        
        events = result.getItems();
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    return events;
}
```

**Où trouver les données** :
- Google Calendar API : Configuration OAuth2 dans les constantes
- Credentials : `credentials.json` (doit être créé)
- Tokens : Stockés dans le dossier `tokens/`
- Séances : Passées en paramètre depuis les contrôleurs

---

### 3️⃣ Entités (Data Models)

#### Seance.java
**Emplacement** : `src/main/java/piJava/entities/Seance.java`

**Attributs utilisés pour l'agenda** :

```java
// Ligne 15-40
private Integer id;
private Integer matiereId;     // Pour le titre de l'événement
private Integer salleId;       // Pour le lieu
private String typeSeance;     // Pour le titre
private String mode;           // Présentiel / En ligne
private Timestamp heureDebut;  // Pour DTSTART
private Timestamp heureFin;    // Pour DTEND
private Integer classeId;      // Pour la description
```

---

#### user.java
**Emplacement** : `src/main/java/piJava/entities/user.java`

**Attributs utilisés pour l'agenda** :

```java
// Ligne 15-35
private Integer id;
private Integer classe_id;     // Pour la description
private String email;         // Pour l'authentification OAuth2
```

---

### 4️⃣ Fichiers de Configuration

#### credentials.json (à créer)
**Emplacement** : `src/main/resources/credentials.json`

**Format** : Fichier JSON fourni par Google Cloud Console

```json
{
  "installed": {
    "client_id": "votre_client_id.apps.googleusercontent.com",
    "project_id": "votre_projet_id",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "votre_client_secret",
    "redirect_uris": ["http://localhost:8888/Callback"]
  }
}
```

---

### 5️⃣ Fichiers FXML (UI Views)

#### EmploiContent.fxml
**Emplacement** : `src/main/resources/frontoffice/emploi/EmploiContent.fxml`

**Boutons pour l'agenda** :

```xml
<!-- Ligne 30-35 : Bouton Ajouter à Google Calendar -->
<Button fx:id="addToAgendaButton" text="Ajouter à Google Calendar" onAction="#addToGoogleCalendar" styleClass="calendar-button"/>

<!-- Ligne 40-45 : Bouton Export ICS -->
<Button fx:id="exportICSButton" text="Exporter ICS" onAction="#exportToICS" styleClass="ics-button"/>
```

---

#### SeanceContent.fxml
**Emplacement** : `src/main/resources/backoffice/Seance/SeanceContent.fxml`

**Bouton pour l'agenda** :

```xml
<!-- Ligne 55-60 : Bouton Ajouter à l'agenda -->
<Button text="Ajouter à l'Agenda" onAction="#addSeanceToAgenda" styleClass="calendar-button"/>

<!-- Ligne 65-70 : Bouton Export ICS -->
<Button text="Exporter ICS" onAction="#exportSeanceToICS" styleClass="ics-button"/>
```

---

### 6️⃣ Styles CSS

#### emploi.css
**Emplacement** : `src/main/resources/frontoffice/emploi/emploi.css`

**Styles pour les boutons d'agenda** :

```css
/* Ligne 30-45 */
.calendar-button {
    -fx-background-color: #ea4335;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-cursor: hand;
    -fx-padding: 10 20;
}

.ics-button {
    -fx-background-color: #34a853;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-cursor: hand;
    -fx-padding: 10 20;
}
```

---

## 📊 Flux de Données

### Ajout à Google Calendar
```
[Utilisateur clique sur "Ajouter à Google Calendar"]
        ↓
[EmploiContentController.addToGoogleCalendar()]
        ↓
[SeanceService.getAllSeances()]
        ↓
[Filtrage par classe utilisateur]
        ↓
[GoogleCalendarService.addSeanceToCalendar()]
        ↓
[MatiereService.getById()] → Nom matière
        ↓
[SalleService.getById()] → Nom salle
        ↓
[Google Calendar API]
        ↓
[Événement créé dans le calendrier]
```

### Export ICS
```
[Utilisateur clique sur "Exporter ICS"]
        ↓
[EmploiContentController.exportToICS()]
        ↓
[SeanceService.getAllSeances()]
        ↓
[Filtrage par classe utilisateur]
        ↓
[GoogleCalendarService.exportToICS()]
        ↓
[Fichier ICS généré]
        ↓
[Téléchargement du fichier]
```

---

## 🔍 Comment Trouver les Données

### Pour configurer Google Calendar API :
1. **Créer** un projet sur Google Cloud Console
2. **Activer** Google Calendar API
3. **Créer** des credentials OAuth 2.0
4. **Télécharger** le fichier `credentials.json`
5. **Placer** le fichier dans `src/main/resources/`

### Pour modifier le format des événements Google Calendar :
1. **Fichier** : `src/main/java/piJava/services/GoogleCalendarService.java`
2. **Méthode** : `addSeanceToCalendar()` (lignes 145-200)
3. **Éléments modifiables** :
   - `setSummary()` : Titre de l'événement
   - `setDescription()` : Description
   - `setLocation()` : Lieu
   - Reminders : Minutes avant l'événement

### Pour modifier le format du fichier ICS :
1. **Fichier** : `src/main/java/piJava/services/GoogleCalendarService.java`
2. **Méthode** : `exportToICS()` (lignes 205-280)
3. **Éléments modifiables** :
   - `SUMMARY` : Titre
   - `DESCRIPTION` : Description
   - `LOCATION` : Lieu
   - `DTSTART` / `DTEND` : Horaires
   - Rappels dans `VALARM`

### Pour ajouter des rappels personnalisés :
1. **Fichier** : `src/main/java/piJava/services/GoogleCalendarService.java`
2. **Méthode** : `addSeanceToCalendar()`
3. **Modifier** : Le tableau `reminders` (lignes 190-194)

---

## 📄 Format du Fichier ICS

```ics
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//EspritFlow//EspritFlow Calendar//EN
CALSCALE:GREGORIAN
METHOD:PUBLISH
BEGIN:VEVENT
UID:uuid@espritflow.tn
SUMMARY:Matière - Type
DESCRIPTION:Classe : X\\nSalle : A101\\nMode : Présentiel
LOCATION:ESPRIT - A101
DTSTART:20260430T080000
DTEND:20260430T090000
BEGIN:VALARM
TRIGGER:-PT15M
ACTION:DISPLAY
DESCRIPTION:Reminder
END:VALARM
END:VEVENT
END:VCALENDAR
```

---

## 🚀 Points d'Entrée

### Frontoffice
- **Menu Emploi** → Bouton "Ajouter à Google Calendar" → Ajout de toutes les séances
- **Menu Emploi** → Bouton "Exporter ICS" → Téléchargement du fichier ICS

### Backoffice
- **Menu Séances** → Sélectionner une séance → Bouton "Ajouter à l'Agenda" → Ajout d'une séance
- **Menu Séances** → Sélectionner une séance → Bouton "Exporter ICS" → Téléchargement du fichier ICS

---

**Dernière mise à jour** : 30 avril 2026
