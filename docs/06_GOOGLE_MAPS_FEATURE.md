# 🗺️ Documentation Fonctionnalité : Google Maps Intégrée

## 📋 Vue d'ensemble

La fonctionnalité Google Maps intégrée permet d'afficher une carte interactive du campus ESPRIT avec les blocs de salles, la position GPS de l'utilisateur en temps réel, et la possibilité de réserver des salles directement depuis la carte.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **JavaScript / HTML5**
- **Google Maps JavaScript API** : Affichage de la carte
  - `google.maps.Map` : Instance de carte
  - `google.maps.Marker` : Marqueurs sur la carte
  - `google.maps.InfoWindow` : Infobulles
  - `google.maps.LatLng` : Coordonnées GPS
  - `google.maps.SymbolPath` : Formes personnalisées
  - `google.maps.ImageMapType` : Types de cartes personnalisés

### 2. **JavaFX WebView**
- **WebView** : Composant JavaFX pour afficher du HTML/JavaScript
  - `javafx.scene.web.WebEngine` : Moteur de rendu
  - `javafx.scene.web.WebView` : Conteneur UI
  - `netscape.javascript.JSObject` : Pont Java↔JavaScript

### 3. **Geolocation API**
- **HTML5 Geolocation** : Position GPS de l'utilisateur
  - `navigator.geolocation.getCurrentPosition()` : Position unique
  - `navigator.geolocation.watchPosition()` : Position en temps réel
  - `Position.coords.latitude` : Latitude
  - `Position.coords.longitude` : Longitude

### 4. **OpenStreetMap**
- **OSM Tiles** : Tuiles de carte gratuites
  - Remplace les tuiles Google Maps (évite les bugs JavaFX)
  - URL : `https://tile.openstreetmap.org/{z}/{x}/{y}.png`

---

## 🔧 Architecture du Système

### 1. **Structure des Fichiers**

```
FrontCampusMap.fxml
├── WebView (affiche map.html)
└── SidePanel (panneau latéral pour réservation)

map.html
├── Google Maps API
├── Configuration des marqueurs
├── Geolocation
└── Communication avec JavaFX

FrontCampusMapController.java
├── WebEngine (pont Java↔JS)
├── Gestion de la sélection de bloc
├── Affichage des salles disponibles
└── Réservation de créneaux
```

### 2. **Flux de Données**

```
JavaFX Controller
    ↓
WebEngine.load(map.html)
    ↓
JavaScript Google Maps API
    ↓
Affichage carte + marqueurs
    ↓
Géolocalisation (HTML5)
    ↓
JavaConnector (JS→Java)
    ↓
Traitement côté Java
```

---

## 💻 Implémentation Technique

### 1. **Fichier HTML de la Carte**

```html
<!DOCTYPE html>
<html>
<head>
    <title>Campus Map - ESPRIT</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <style>
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
            background-color: transparent;
        }

        #map {
            height: 100%;
            width: 100%;
            border-radius: 15px;
        }
    </style>
</head>
<body>
    <div id="map"></div>

    <script>
        // Variables globales
        let map;
        let userMarker;
        let userWatchId;

        // Coordonnées des blocs ESPRIT
        const locations = [
            { name: "Bloc A / B / C", lat: 36.898482, lng: 10.189762, color: "#ef4444" },
            { name: "Bloc D", lat: 36.899313, lng: 10.188764, color: "#f59e0b" },
            { name: "Bloc E", lat: 36.899738, lng: 10.189332, color: "#10b981" },
            { name: "Bloc G", lat: 36.898124, lng: 10.187311, color: "#3b82f6" },
            { name: "Bloc H", lat: 36.895393, lng: 10.188686, color: "#8b5cf6" },
            { name: "Bloc I / J / K", lat: 36.901173, lng: 10.190473, color: "#ec4899" }
        ];

        /**
         * Initialise la carte Google Maps
         */
        function initMap() {
            try {
                // Centre du campus ESPRIT
                const espritCenter = { lat: 36.898, lng: 10.189 };

                // 1. Initialisation de la carte via Google Maps API
                map = new google.maps.Map(document.getElementById("map"), {
                    center: espritCenter,
                    zoom: 16,
                    mapTypeControl: false,
                    streetViewControl: false,
                    fullscreenControl: false,
                    backgroundColor: 'none'
                });

                // 2. CONTOURNEMENT DU BUG JAVAFX :
                // Remplacement des tuiles Google par OpenStreetMap
                const osmMapType = new google.maps.ImageMapType({
                    getTileUrl: function(coord, zoom) {
                        return "https://tile.openstreetmap.org/" + 
                               zoom + "/" + coord.x + "/" + coord.y + ".png";
                    },
                    tileSize: new google.maps.Size(256, 256),
                    maxZoom: 19,
                    name: "OSM"
                });

                map.mapTypes.set('osm', osmMapType);
                map.setMapTypeId('osm');

                // 3. Ajouter les marqueurs de blocs
                addBlockMarkers();

                // 4. Activer la géolocalisation
                enableUserGeolocation();

            } catch (e) {
                showError("Erreur de chargement Google Maps.");
            }
        }

        /**
         * Ajoute les marqueurs pour chaque bloc
         */
        function addBlockMarkers() {
            const infoWindow = new google.maps.InfoWindow();

            locations.forEach(loc => {
                const marker = new google.maps.Marker({
                    position: { lat: loc.lat, lng: loc.lng },
                    map: map,
                    title: loc.name,
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 10,
                        fillColor: loc.color,
                        fillOpacity: 1,
                        strokeWeight: 2,
                        strokeColor: "#ffffff"
                    }
                });

                // Événement clic sur le marqueur
                marker.addListener("click", () => {
                    infoWindow.setContent(`<div style='font-weight:bold; padding:5px;'>${loc.name}</div>`);
                    infoWindow.open(map, marker);

                    // Communiquer avec JavaFX Controller
                    if (typeof javaConnector !== 'undefined') {
                        javaConnector.onBlockClicked(loc.name);
                    }
                });
            });
        }

        /**
         * Active la géolocalisation de l'utilisateur
         */
        function enableUserGeolocation() {
            
            if (navigator.geolocation) {
                
                // Position actuelle
                navigator.geolocation.getCurrentPosition(
                    (position) => updateUserPosition(position.coords.latitude, position.coords.longitude),
                    (error) => console.log("Erreur de géolocalisation: " + error.message),
                    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
                );

                // Position en temps réel
                userWatchId = navigator.geolocation.watchPosition(
                    (position) => updateUserPosition(position.coords.latitude, position.coords.longitude),
                    (error) => console.log("Erreur de mise à jour: " + error.message),
                    { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 }
                );
            }
        }

        /**
         * Met à jour le marqueur de position de l'utilisateur
         */
        function updateUserPosition(lat, lng) {
            
            const userIcon = {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 12,
                fillColor: "#0066ff",  // Bleu
                fillOpacity: 1,
                strokeWeight: 3,
                strokeColor: "#ffffff"
            };

            if (userMarker) {
                // Mettre à jour la position existante
                userMarker.setPosition({ lat: lat, lng: lng });
                userMarker.setIcon(userIcon);
            } else {
                // Créer un nouveau marqueur
                userMarker = new google.maps.Marker({
                    position: { lat: lat, lng: lng },
                    map: map,
                    title: "Votre position actuelle",
                    icon: userIcon,
                    zIndex: 1000,
                    animation: google.maps.Animation.DROP
                });

                // Infobulle automatique
                const userContent = '<div style="font-weight:bold; padding:5px;">📍 Votre position actuelle</div>';
                const userWindow = new google.maps.InfoWindow({ content: userContent });
                userMarker.addListener("click", () => userWindow.open(map, userMarker));
                userWindow.open(map, userMarker);
            }

            // Centrer la carte sur l'utilisateur
            map.setCenter({ lat: lat, lng: lng });
            map.setZoom(17);
        }

        /**
         * Gestion des erreurs
         */
        function gm_authFailure() {
            document.getElementById('map').innerHTML = 
                "<div style='display:flex; justify-content:center; align-items:center; height:100%; background-color:#f8d7da; color:#721c24; padding:20px; text-align:center; border-radius:15px;'>" +
                "Erreur d'authentification Google Maps.<br>La clé d'API est invalide." +
                "</div>";
        }

        function showError(message) {
            document.getElementById('map').innerHTML = 
                "<div style='display:flex; justify-content:center; align-items:center; height:100%; background-color:#f8d7da; color:#721c24; padding:20px; text-align:center; border-radius:15px;'>" +
                message +
                "</div>";
        }
    </script>

    <!-- Chargement de l'API Google Maps avec clé -->
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA-rJTi6eTMy9EujIPFbhtAY5Lv3sPzDnA&callback=initMap"></script>
</body>
</html>
```

---

### 2. **Contrôleur JavaFX**

```java
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 * Contrôleur pour la carte du campus
 */
public class FrontCampusMapController implements Initializable {

    @FXML private WebView webView;
    @FXML private VBox sidePanel;
    @FXML private VBox roomsContainer;
    @FXML private Label lblBlockTitle;

    private WebEngine webEngine;
    private SalleService salleService = new SalleService();
    private SeanceService seanceService = new SeanceService();

    // Créneaux horaires standard ESPRIT
    private final String[][] SLOTS = {
        {"09:00", "10:30"},
        {"10:45", "12:15"},
        {"13:30", "15:00"},
        {"15:15", "16:45"}
    };

    // Référence forte pour éviter Garbage Collection du JavaConnector
    private JavaConnector myJavaConnector = new JavaConnector();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Cacher le panneau latéral au démarrage
        sidePanel.setVisible(false);
        sidePanel.setManaged(false);

        // Initialiser le WebEngine
        webEngine = webView.getEngine();
        
        // Charger le fichier HTML de la carte
        URL mapUrl = getClass().getResource("/frontoffice/salle/map.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("Map HTML not found.");
        }

        // Exposer l'objet Java au JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", myJavaConnector);
            }
        });
    }

    /**
     * Classe pont JavaScript → Java
     */
    public class JavaConnector {
        /**
         * Appelé quand un bloc est cliqué sur la carte
         */
        public void onBlockClicked(String blockName) {
            Platform.runLater(() -> handleBlockSelection(blockName));
        }
    }

    /**
     * Gère la sélection d'un bloc
     */
    private void handleBlockSelection(String blockName) {
        
        lblBlockTitle.setText(blockName);
        sidePanel.setVisible(true);
        sidePanel.setManaged(true);
        
        // Parser les lettres de bloc (ex: "Bloc A / B / C" → ["A", "B", "C"])
        String[] targets = blockName.replace("Bloc", "").replace(" ", "").split("/");
        
        try {
            List<Salle> allSalles = salleService.getAllSalles();
            List<Salle> matchingSalles = new ArrayList<>();
            
            // Filtrer les salles du bloc
            for (Salle s : allSalles) {
                for (String t : targets) {
                    if (s.getBlock().equalsIgnoreCase(t)) {
                        matchingSalles.add(s);
                        break;
                    }
                }
            }
            
            // Afficher les salles disponibles
            displayRooms(matchingSalles);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

### 3. **Affichage des Salles et Créneaux**

```java
/**
 * Affiche les salles du bloc sélectionné avec les créneaux disponibles
 */
private void displayRooms(List<Salle> salles) {
    
    roomsContainer.getChildren().clear();

    if (salles.isEmpty()) {
        roomsContainer.getChildren().add(new Label("Aucune salle trouvée."));
        return;
    }

    // Récupérer les 6 prochains jours
    LocalDate today = LocalDate.now();
    List<LocalDate> weekDays = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
        if (today.plusDays(i).getDayOfWeek() != DayOfWeek.SUNDAY) {
            weekDays.add(today.plusDays(i));
        }
    }

    // Pour chaque salle
    for (Salle salle : salles) {
        
        VBox salleBox = createSalleBox(salle);
        
        // Pour chaque jour
        for (LocalDate date : weekDays) {
            
            Label lDate = new Label(date.format(DateTimeFormatter.ofPattern("EEEE dd/MM")));
            lDate.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
            salleBox.getChildren().add(lDate);
            
            // Pour chaque créneau horaire
            for (String[] slot : SLOTS) {
                
                LocalDateTime startDT = LocalDateTime.of(date, LocalTime.parse(slot[0]));
                LocalDateTime endDT = LocalDateTime.of(date, LocalTime.parse(slot[1]));
                
                // Ignorer le passé
                if (startDT.isBefore(LocalDateTime.now())) continue;
                
                // Vérifier la disponibilité
                boolean isFree = checkAvailability(salle.getId(), startDT, endDT);
                
                // Créer l'UI du créneau
                HBox slotBox = createSlotBox(salle, startDT, endDT, isFree, slot);
                salleBox.getChildren().add(slotBox);
            }
        }
        
        roomsContainer.getChildren().add(salleBox);
    }
}

/**
 * Vérifie si une salle est libre pour un créneau
 */
private boolean checkAvailability(int salleId, LocalDateTime start, LocalDateTime end) {
    
    try {
        List<Seance> seances = seanceService.getAllSeances();
        
        for (Seance s : seances) {
            if (s.getSalleId() != salleId) continue;
            if (s.getHeureDebut() == null || s.getHeureFin() == null) continue;
            
            LocalDateTime sStart = s.getHeureDebut().toLocalDateTime();
            LocalDateTime sEnd = s.getHeureFin().toLocalDateTime();
            
            // Vérifier le chevauchement
            if (start.isBefore(sEnd) && end.isAfter(sStart)) {
                return false;  // Occupé
            }
        }
        
        return true;  // Libre
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

/**
 * Crée la box d'un créneau horaire
 */
private HBox createSlotBox(Salle salle, LocalDateTime start, LocalDateTime end, 
                          boolean isFree, String[] slot) {
    
    HBox slotBox = new HBox(10);
    slotBox.setAlignment(Pos.CENTER_LEFT);
    slotBox.setStyle(
        "-fx-background-color: " + (isFree ? "#ecfdf5" : "#fee2e2") + "; " +
        "-fx-padding: 10; -fx-background-radius: 8; " +
        "-fx-border-color: " + (isFree ? "#a7f3d0" : "#fca5a5") + "; " +
        "-fx-border-radius: 8;"
    );

    // Infos horaires
    VBox timeInfos = new VBox(2);
    Label lTime = new Label(slot[0] + " - " + slot[1]);
    lTime.setStyle("-fx-font-weight: bold;");
    
    Label lStat = new Label(isFree ? "✔ Disponible" : "✖ Occupé");
    lStat.setStyle("-fx-font-size: 11px; -fx-text-fill: " + 
                  (isFree ? "#059669" : "#ef4444") + "; -fx-font-weight: bold;");
    
    timeInfos.getChildren().addAll(lTime, lStat);
    slotBox.getChildren().add(timeInfos);

    // Spacer
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    slotBox.getChildren().add(spacer);

    // Bouton de réservation
    if (isFree) {
        Button btnReserve = new Button("Réserver");
        btnReserve.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-background-radius: 5;");
        btnReserve.setOnAction(e -> bookSlot(salle, start, end));
        slotBox.getChildren().add(btnReserve);
    } else {
        Label lblOcc = new Label("Indisponible");
        lblOcc.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
        slotBox.getChildren().add(lblOcc);
    }

    return slotBox;
}
```

---

### 4. **Réservation de Séance de Révision**

```java
/**
 * Réserve un créneau pour une séance de révision
 */
private void bookSlot(Salle salle, LocalDateTime start, LocalDateTime end) {
    
    user currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser == null) {
        showAlert("Erreur", "Vous devez être connecté.");
        return;
    }

    try {
        // Créer la séance de révision
        Seance nouvelleRevision = new Seance();
        
        // Jour en français
        String[] frenchDays = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        int dayIdx = start.getDayOfWeek().getValue() - 1;
        nouvelleRevision.setJour(frenchDays[dayIdx]);
        
        // Type de séance
        nouvelleRevision.setTypeSeance("Révision");
        nouvelleRevision.setMode("Présentiel");
        
        // Horaires
        nouvelleRevision.setHeureDebut(Timestamp.valueOf(start));
        nouvelleRevision.setHeureFin(Timestamp.valueOf(end));
        
        // Salle et classe
        nouvelleRevision.setSalleId(salle.getId());
        nouvelleRevision.setClasseId(currentUser.getClasse_id() != null ? currentUser.getClasse_id() : 1);
        
        // Matière (par défaut 1)
        nouvelleRevision.setMatiereId(1);
        
        // Sauvegarder
        seanceService.add(nouvelleRevision);
        
        // Afficher le succès
        lblActionSuccess.setText("✔ Séance de révision ajoutée — " + 
                              salle.getBlock() + salle.getNumber() + ", " + 
                              start.format(DateTimeFormatter.ofPattern("EEEE HH:mm")));
        lblActionSuccess.setVisible(true);
        
        // Rafraîchir l'affichage
        handleBlockSelection(lblBlockTitle.getText());
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Une erreur est survenue lors de la réservation.");
    }
}
```

---

## 🎨 Personnalisation de la Carte

### 1. **Marqueurs Personnalisés**

```javascript
/**
 * Crée un marqueur avec icône personnalisée
 */
function createCustomMarker(lat, lng, color, label) {
    
    const marker = new google.maps.Marker({
        position: { lat: lat, lng: lng },
        map: map,
        title: label,
        icon: {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 15,
            fillColor: color,
            fillOpacity: 0.8,
            strokeWeight: 2,
            strokeColor: "#ffffff",
            rotation: 0
        },
        label: {
            text: label,
            color: "#ffffff",
            fontSize: "12px",
            fontWeight: "bold"
        }
    });
    
    return marker;
}
```

### 2. **Formes de Marqueurs**

```javascript
/**
 * Différentes formes de marqueurs
 */
const markerShapes = {
    // Cercle (utilisé pour les blocs)
    circle: {
        path: google.maps.SymbolPath.CIRCLE,
        scale: 10
    },
    
    // Étoile (pour les points d'intérêt)
    star: {
        path: 'M 0,-24 6,-8 22,-8 10,2 18,12 2,12 -8,22 -6,8 -6,8 z',
        scale: 0.3,
        fillColor: '#ffff00',
        strokeColor: '#000000',
        strokeWeight: 1
    },
    
    // Pin (goutte d'eau)
    pin: {
        path: 'M 0,0 C -2,0 -4,-3 -4,-6 C -4,-9 0,-10 4,-9 C 4,-9 4,-9 4,-6 C 4,-3 2,0 0,0 z',
        scale: 0.3,
        fillColor: '#ff0000',
        strokeColor: '#000000',
        strokeWeight: 1
    }
};
```

---

## 📊 Géolocalisation Avancée

### 1. **Accuracy et Options**

```javascript
/**
 * Options de géolocalisation avancées
 */
const geoOptions = {
    enableHighAccuracy: true,     // Utiliser GPS haute précision
    timeout: 10000,              // Timeout 10 secondes
    maximumAge: 0                 // Pas de cache, position fraîche
};

// Position unique
navigator.geolocation.getCurrentPosition(success, error, geoOptions);

// Position en temps réel
const watchId = navigator.geolocation.watchPosition(success, error, geoOptions);
```

### 2. **Calcul de Distance**

```javascript
/**
 * Calcule la distance entre deux points (formule Haversine)
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
    
    const R = 6371; // Rayon de la Terre en km
    
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    
    return R * c; // Distance en km
}

function toRad(deg) {
    return deg * (Math.PI/180);
}
```

---

## 🚀 Performance et Optimisation

### 1. **Lazy Loading des Tuiles**

```javascript
/**
 * Configuration de chargement des tuiles optimisé
 */
const osmMapType = new google.maps.ImageMapType({
    getTileUrl: function(coord, zoom) {
        
        // Limiter le zoom pour éviter le chargement excessif
        if (zoom > 18) return '';
        
        return "https://tile.openstreetmap.org/" + 
               zoom + "/" + coord.x + "/" + coord.y + ".png";
    },
    tileSize: new google.maps.Size(256, 256),
    maxZoom: 18,
    minZoom: 14,
    name: "OSM"
});
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **API Carte** | Google Maps JavaScript API |
| **Tuiles** | OpenStreetMap (OSM) |
| **Communication** | JSObject (Java ↔ JavaScript) |
| **Géolocalisation** | HTML5 Geolocation API |
| **Marqueurs** | Symboles personnalisés SVG |
| **Mise à jour** | watchPosition (temps réel) |
| **Réservation** | Séances de révision |
| **UI** | WebView + SidePanel |

---

## 🔗 Ressources

- **Google Maps API** : https://developers.google.com/maps
- **OpenStreetMap** : https://www.openstreetmap.org/
- **Geolocation API** : MDN Web Docs
- **JavaFX WebView** : Oracle Documentation

---

**Documentation générée pour EspritFlow - Google Maps Intégrée** 🗺️
