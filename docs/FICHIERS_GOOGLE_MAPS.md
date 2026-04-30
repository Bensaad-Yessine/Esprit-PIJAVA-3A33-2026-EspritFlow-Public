# 📂 Fichiers Google Maps - Localisation Complète

## 🎯 Vue d'ensemble
Cette fonctionnalité affiche une carte interactive du campus ESPRIT avec la géolocalisation de l'utilisateur et permet de réserver des salles pour des séances de révision.

---

## 📁 Structure des Fichiers

### 1️⃣ Contrôleurs (Controllers)

#### FrontCampusMapController.java
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/salle/FrontCampusMapController.java`

**Responsabilité** : Gestion de la carte interactive et réservation de créneaux

**Méthodes clés** :

```java
// Ligne 30-60 : Déclarations des composants
@FXML
private WebView mapView;
@FXML
private Label lblBlockTitle;
@FXML
private HBox timeSlotsContainer;
@FXML
private Label lblActionSuccess;

// Services
private SeanceService seanceService;
private SalleService salleService;

// Données
private List<Salle> salles = new ArrayList<>();
private LocalDateTime selectedDate = LocalDateTime.now();

// Ligne 70-100 : Initialisation
@Override
public void initialize(URL location, ResourceBundle resources) {
    seanceService = new SeanceService();
    salleService = new SalleService();
    loadMap();
    loadSalles();
}

// Ligne 105-130 : Chargement de la carte HTML
private void loadMap() {
    String mapUrl = getClass().getResource("/frontoffice/salle/map.html").toExternalForm();
    WebEngine webEngine = mapView.getEngine();
    webEngine.load(mapUrl);
    
    // Activer JavaScript
    webEngine.setJavaScriptEnabled(true);
}

// Ligne 135-170 : Chargement des salles depuis la BDD
private void loadSalles() {
    salles = salleService.getAll();
    
    // Grouper par bloc
    Map<String, List<Salle>> blocs = salles.stream()
        .collect(Collectors.groupingBy(Salle::getBlock));
    
    // Créer les boutons de bloc
    for (String bloc : blocs.keySet()) {
        Button btn = new Button(bloc);
        btn.setOnAction(e -> handleBlockSelection(bloc));
        blockButtonsContainer.getChildren().add(btn);
    }
}

// Ligne 175-220 : Sélection d'un bloc
private void handleBlockSelection(String blockName) {
    lblBlockTitle.setText("Bloc " + blockName);
    
    // Filtrer les salles du bloc
    List<Salle> blocSalles = salles.stream()
        .filter(s -> s.getBlock().equals(blockName))
        .collect(Collectors.toList());
    
    // Générer les créneaux horaires pour chaque salle
    timeSlotsContainer.getChildren().clear();
    
    for (Salle salle : blocSalles) {
        generateTimeSlots(salle);
    }
}

// Ligne 225-270 : Génération des créneaux horaires
private void generateTimeSlots(Salle salle) {
    VBox salleBox = new VBox(5);
    salleBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");
    
    Label lblSalle = new Label(salle.getBlock() + salle.getNumber());
    lblSalle.setStyle("-fx-font-weight: bold;");
    
    // Créneaux de 8h à 18h
    for (int hour = 8; hour < 18; hour++) {
        LocalDateTime start = selectedDate.withHour(hour).withMinute(0);
        LocalDateTime end = selectedDate.withHour(hour + 1).withMinute(0);
        
        boolean isAvailable = isSlotAvailable(salle.getId(), start, end);
        
        Button slotBtn = new Button(hour + ":00 - " + (hour + 1) + ":00");
        slotBtn.setStyle(isAvailable ? "-fx-background-color: #10b981;" : "-fx-background-color: #ef4444;");
        slotBtn.setDisable(!isAvailable);
        
        if (isAvailable) {
            slotBtn.setOnAction(e -> bookSlot(salle, start, end));
        }
        
        salleBox.getChildren().add(slotBtn);
    }
    
    timeSlotsContainer.getChildren().add(salleBox);
}

// Ligne 275-300 : Vérification de disponibilité
private boolean isSlotAvailable(int salleId, LocalDateTime start, LocalDateTime end) {
    try {
        List<Seance> seances = seanceService.getBySalleAndDate(
            salleId, 
            Timestamp.valueOf(start), 
            Timestamp.valueOf(end)
        );
        return seances.isEmpty();
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 305-340 : Réservation d'un créneau
private void bookSlot(Salle salle, LocalDateTime start, LocalDateTime end) {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser == null) {
        showAlert("Erreur", "Vous devez être connecté pour réserver.");
        return;
    }

    try {
        Seance nouvelleRevision = new Seance();
        // Defaults for a revision
        String[] frenchDays = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        int dayIdx = start.getDayOfWeek().getValue() - 1;
        nouvelleRevision.setJour(frenchDays[dayIdx]);
        
        nouvelleRevision.setTypeSeance("Révision"); // Type: Révision
        nouvelleRevision.setMode("Présentiel");
        nouvelleRevision.setHeureDebut(Timestamp.valueOf(start));
        nouvelleRevision.setHeureFin(Timestamp.valueOf(end));
        nouvelleRevision.setSalleId(salle.getId());
        nouvelleRevision.setClasseId(currentUser.getClasse_id() != null ? currentUser.getClasse_id() : 1);
        nouvelleRevision.setMatiereId(1); 
        
        seanceService.add(nouvelleRevision);
        
        lblActionSuccess.setText("✔ Séance de révision ajoutée — " + salle.getBlock() + salle.getNumber() + ", " + start.format(DateTimeFormatter.ofPattern("EEEE HH:mm")));
        lblActionSuccess.setVisible(true);
        
        // Refresh
        handleBlockSelection(lblBlockTitle.getText());
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Une erreur est survenue lors de la réservation.");
    }
}
```

**Où trouver les données** :
- Salles : `src/main/java/piJava/services/SalleService.java`
- Séances : `src/main/java/piJava/services/SeanceService.java`
- Utilisateur : `SessionManager.getInstance().getCurrentUser()`
- Carte HTML : `src/main/resources/frontoffice/salle/map.html`

---

#### FrontSallesController.java
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/salle/FrontSallesController.java`

**Responsabilité** : Liste des salles avec accès à la carte

**Méthodes clés** :

```java
// Ligne 250-270 : Gestion du clic sur le bouton Map
@FXML
private void handleMapClick() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/salle/FrontCampusMap.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Carte du Campus");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

**Où trouver les données** :
- FXML carte : `src/main/resources/frontoffice/salle/FrontCampusMap.fxml`

---

### 2️⃣ Fichiers HTML/JavaScript (Map)

#### map.html
**Emplacement** : `src/main/resources/frontoffice/salle/map.html`

**Responsabilité** : Affichage de la carte Google Maps et géolocalisation

**Code JavaScript clé** :

```javascript
// Ligne 10-30 : Variables globales
let map;
let userMarker; // Marker pour la position actuelle de l'utilisateur
let userWatchId; // ID pour le watchPosition (mise à jour en temps réel)

// Coordonnées des blocs ESPRIT
const locations = [
    { name: "Bloc A / B / C", lat: 36.898482, lng: 10.189762, color: "#ef4444" },
    { name: "Bloc D", lat: 36.899313, lng: 10.188764, color: "#f59e0b" },
    { name: "Bloc E", lat: 36.899738, lng: 10.189332, color: "#10b981" },
    { name: "Bloc G", lat: 36.898124, lng: 10.187311, color: "#3b82f6" },
    { name: "Bloc H", lat: 36.895393, lng: 10.188686, color: "#8b5cf6" },
    { name: "Bloc I / J / K", lat: 36.901173, lng: 10.190473, color: "#ec4899" }
];

// Ligne 35-80 : Initialisation de la carte
function initMap() {
    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 36.8985, lng: 10.1890 }, // Centre sur Esprit
        zoom: 16,
        mapTypeControl: false,
        streetViewControl: false,
        fullscreenControl: false
    });

    // Ajout de la couche OpenStreetMap
    const osmMapType = new google.maps.ImageMapType({
        getTileUrl: function(coord, zoom) {
            return "https://a.tile.openstreetmap.org/" + zoom + "/" + coord.x + "/" + coord.y + ".png";
        },
        tileSize: new google.maps.Size(256, 256),
        name: "OSM",
        maxZoom: 18
    });
    
    map.mapTypes.set('osm', osmMapType);
    map.setMapTypeId('osm');

    const infoWindow = new google.maps.InfoWindow();

    // Ligne 85-150 : Géolocalisation de l'utilisateur
    function updateUserPosition(lat, lng) {
        const userIcon = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 12,
            fillColor: "#0066ff", // Bleu
            fillOpacity: 1,
            strokeWeight: 3,
            strokeColor: "#ffffff"
        };

        if (userMarker) {
            userMarker.setPosition({ lat: lat, lng: lng });
            userMarker.setIcon(userIcon);
            userMarker.setTitle("Votre position actuelle");
        } else {
            userMarker = new google.maps.Marker({
                position: { lat: lat, lng: lng },
                map: map,
                title: "Votre position actuelle",
                icon: userIcon,
                zIndex: 1000,
                animation: google.maps.Animation.DROP
            });

            const userContent = '<div style="font-weight:bold; padding:5px;">📍 Votre position actuelle</div>';
            const userWindow = new google.maps.InfoWindow({ content: userContent });
            userMarker.addListener("click", () => {
                userWindow.open(map, userMarker);
            });
            userWindow.open(map, userMarker);
        }

        map.setCenter({ lat: lat, lng: lng });
        map.setZoom(17);
    }

    // Ligne 155-200 : Récupération de la position GPS
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                updateUserPosition(position.coords.latitude, position.coords.longitude);
            },
            (error) => {
                console.log("Erreur de géolocalisation: " + error.message);
                alert("Impossible d'obtenir votre position GPS.");
            },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
        );

        // Mise à jour en temps réel
        userWatchId = navigator.geolocation.watchPosition(
            (position) => {
                updateUserPosition(position.coords.latitude, position.coords.longitude);
            },
            (error) => {
                console.log("Erreur de mise à jour de position: " + error.message);
            },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 }
        );
    }

    // Ligne 205-250 : Création des marqueurs pour les blocs
    locations.forEach(loc => {
        const marker = new google.maps.Marker({
            position: { lat: loc.lat, lng: loc.lng },
            map: map,
            title: loc.name,
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 15,
                fillColor: loc.color,
                fillOpacity: 0.8,
                strokeWeight: 2,
                strokeColor: "#ffffff"
            },
            animation: google.maps.Animation.DROP
        });

        const content = `
            <div style="padding: 10px;">
                <h3 style="margin: 0 0 10px 0; color: ${loc.color};">${loc.name}</h3>
                <p style="margin: 0;">Lat: ${loc.lat.toFixed(6)} | Lng: ${loc.lng.toFixed(6)}</p>
                <button onclick="selectBloc('${loc.name}')" style="margin-top: 10px; padding: 5px 10px; cursor: pointer; background-color: ${loc.color}; color: white; border: none; border-radius: 5px;">
                    Voir les salles
                </button>
            </div>
        `;

        marker.addListener("click", () => {
            infoWindow.setContent(content);
            infoWindow.open(map, marker);
        });
    });
}

// Ligne 255-270 : Sélection d'un bloc depuis la carte
function selectBloc(blockName) {
    if (window.JavaFX) {
        window.JavaFX.selectBloc(blockName);
    }
}
```

**Où trouver les données** :
- Coordonnées GPS des blocs : Définies dans le tableau `locations`
- Position utilisateur : Récupérée via `navigator.geolocation`
- Google Maps API : Nécessite une clé API (définie dans le script de chargement)

---

### 3️⃣ Services (Business Logic)

#### SeanceService.java
**Emplacement** : `src/main/java/piJava/services/SeanceService.java`

**Méthodes utilisées pour la carte** :

```java
// Ligne 200-230 : Récupérer les séances d'une salle entre deux dates
public List<Seance> getBySalleAndDate(int salleId, Timestamp start, Timestamp end) {
    List<Seance> list = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM seance WHERE salle_id = ? AND " +
                     "((heure_debut >= ? AND heure_debut < ?) OR " +
                     "(heure_fin > ? AND heure_fin <= ?) OR " +
                     "(heure_debut <= ? AND heure_fin >= ?))";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, salleId);
        stmt.setTimestamp(2, start);
        stmt.setTimestamp(3, end);
        stmt.setTimestamp(4, start);
        stmt.setTimestamp(5, end);
        stmt.setTimestamp(6, start);
        stmt.setTimestamp(7, end);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Seance s = new Seance();
            // Mapping des champs
            list.add(s);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
```

**Où trouver les données** :
- Table BDD : `seance`
- Colonnes utilisées : `id`, `salle_id`, `heure_debut`, `heure_fin`

---

#### SalleService.java
**Emplacement** : `src/main/java/piJava/services/SalleService.java`

**Méthodes utilisées pour la carte** :

```java
// Ligne 50-80 : Récupérer toutes les salles
public List<Salle> getAll() {
    List<Salle> list = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM salle";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            Salle s = new Salle();
            s.setId(rs.getInt("id"));
            s.setBlock(rs.getString("block"));
            s.setNumber(rs.getString("number"));
            s.setCapacity(rs.getInt("capacity"));
            list.add(s);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
```

**Où trouver les données** :
- Table BDD : `salle`
- Colonnes utilisées : `id`, `block`, `number`, `capacity`

---

### 4️⃣ Entités (Data Models)

#### Salle.java
**Emplacement** : `src/main/java/piJava/entities/Salle.java`

**Attributs utilisés pour la carte** :

```java
// Ligne 10-25
private Integer id;
private String block;       // Bloc (A, B, C, D, E, G, H, I, J, K)
private String number;      // Numéro de salle (ex: "101")
private Integer capacity;   // Capacité de la salle
```

---

#### Seance.java
**Emplacement** : `src/main/java/piJava/entities/Seance.java`

**Attributs utilisés pour la carte** :

```java
// Ligne 15-40
private Integer id;
private Integer salleId;     // Pour vérifier la disponibilité
private String typeSeance;   // "Révision" pour les réservations depuis la carte
private String jour;
private Timestamp heureDebut;
private Timestamp heureFin;
private String mode;
```

---

### 5️⃣ Fichiers FXML (UI Views)

#### FrontCampusMap.fxml
**Emplacement** : `src/main/resources/frontoffice/salle/FrontCampusMap.fxml`

**Structure de l'interface** :

```xml
<!-- Ligne 10-40 : Conteneur principal -->
<VBox spacing="10" styleClass="map-container">
    
    <!-- Ligne 45-60 : En-tête avec titre du bloc -->
    <HBox spacing="10" alignment="CENTER_LEFT">
        <Label fx:id="lblBlockTitle" text="Sélectionnez un bloc" styleClass="map-title"/>
    </HBox>
    
    <!-- Ligne 65-85 : Carte WebView -->
    <WebView fx:id="mapView" prefHeight="400" styleClass="map-view"/>
    
    <!-- Ligne 90-120 : Boutons de sélection de bloc -->
    <HBox fx:id="blockButtonsContainer" spacing="10" styleClass="block-buttons"/>
    
    <!-- Ligne 125-150 : Créneaux horaires -->
    <ScrollPane fitToWidth="true">
        <VBox fx:id="timeSlotsContainer" spacing="10"/>
    </ScrollPane>
    
    <!-- Ligne 155-160 : Message de succès -->
    <Label fx:id="lblActionSuccess" text="" styleClass="success-message" visible="false"/>
</VBox>
```

---

#### FrontSallesContent.fxml
**Emplacement** : `src/main/resources/frontoffice/salle/FrontSallesContent.fxml`

**Bouton pour accéder à la carte** :

```xml
<!-- Ligne 30-35 : Bouton Map -->
<Button text="Map" onAction="#handleMapClick" styleClass="map-button"/>
```

---

### 6️⃣ Styles CSS

#### front_salles.css
**Emplacement** : `src/main/resources/frontoffice/salle/front_salles.css`

**Styles pour la carte** :

```css
/* Ligne 15-30 : Conteneur carte */
.map-container {
    -fx-padding: 20;
    -fx-spacing: 15;
}

.map-title {
    -fx-font-size: 20px;
    -fx-font-weight: bold;
    -fx-text-fill: #333;
}

.map-view {
    -fx-border-color: #ddd;
    -fx-border-radius: 10;
    -fx-border-width: 2;
}

.map-button {
    -fx-background-color: #2563eb;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-cursor: hand;
}

.block-buttons {
    -fx-padding: 10;
    -fx-spacing: 10;
}

.success-message {
    -fx-text-fill: #10b981;
    -fx-font-weight: bold;
}
```

---

## 📊 Flux de Données

### Affichage de la carte
```
[FrontCampusMapController.initialize()]
        ↓
[Chargement map.html]
        ↓
[JavaScript: initMap()]
        ↓
[Google Maps API]
        ↓
[navigator.geolocation] → Position utilisateur
        ↓
[Affichage marqueurs blocs + marqueur utilisateur]
```

### Réservation d'une salle
```
[Utilisateur clique sur créneau]
        ↓
[FrontCampusMapController.bookSlot()]
        ↓
[Vérification disponibilité: SeanceService.getBySalleAndDate()]
        ↓
[Création Seance.setTypeSeance("Révision")]
        ↓
[SeanceService.add()]
        ↓
[BDD: Table seance]
        ↓
[Mise à jour interface: handleBlockSelection()]
```

---

## 🔍 Comment Trouver les Données

### Pour modifier les coordonnées des blocs :
1. **Fichier** : `src/main/resources/frontoffice/salle/map.html`
2. **Variable** : `locations` (lignes 14-21)
3. **Format** : `{ name: "Nom", lat: 36.xxxxx, lng: 10.xxxxx, color: "#hex" }`

### Pour modifier le style du marqueur utilisateur :
1. **Fichier** : `src/main/resources/frontoffice/salle/map.html`
2. **Fonction** : `updateUserPosition()` (lignes 85-120)
3. **Propriétés** : `fillColor` (couleur), `scale` (taille)

### Pour modifier les créneaux horaires disponibles :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/salle/FrontCampusMapController.java`
2. **Méthode** : `generateTimeSlots()` (lignes 225-270)
3. **Boucle** : Modifier la boucle `for (int hour = 8; hour < 18; hour++)`

### Pour modifier la disponibilité d'une salle :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/salle/FrontCampusMapController.java`
2. **Méthode** : `isSlotAvailable()` (lignes 275-300)
3. **Logique** : Modifier la requête SQL dans `SeanceService.getBySalleAndDate()`

---

## 🗺️ Coordonnées GPS des Blocs ESPRIT

| Bloc | Latitude | Longitude | Couleur |
|------|----------|-----------|---------|
| A / B / C | 36.898482 | 10.189762 | #ef4444 (rouge) |
| D | 36.899313 | 10.188764 | #f59e0b (orange) |
| E | 36.899738 | 10.189332 | #10b981 (vert) |
| G | 36.898124 | 10.187311 | #3b82f6 (bleu) |
| H | 36.895393 | 10.188686 | #8b5cf6 (violet) |
| I / J / K | 36.901173 | 10.190473 | #ec4899 (rose) |

---

## 🚀 Points d'Entrée

### Frontoffice
- **Menu Salles** → Bouton "Map" → Ouverture de la carte
- **Menu Sidebar** → "Campus Map" → Ouverture directe de la carte
- **Géolocalisation** : Activée automatiquement à l'ouverture de la carte
- **Réservation** : Clic sur un créneau horaire disponible (vert)

---

**Dernière mise à jour** : 30 avril 2026
