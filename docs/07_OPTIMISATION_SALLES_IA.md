# 🤖 Documentation Fonctionnalité : Optimisation des Salles par IA

## 📋 Vue d'ensemble

La fonctionnalité d'optimisation des salles par Intelligence Artificielle utilise des algorithmes d'apprentissage automatique pour suggérer la meilleure salle pour une séance en fonction de multiples critères : capacité, disponibilité, préférences historiques, équipment spécialisé, et proximité géographique.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **Bibliothèques Java**
- **Weka (Waikato Environment for Knowledge Analysis)** : Machine Learning en Java
  - `weka.classifiers.*` : Algorithmes de classification
  - `weka.clusterers.*` : Algorithmes de clustering
  - `weka.core.*` : Structures de données
- **Apache Commons Math** : Calculs mathématiques avancés
  - `org.apache.commons.math3.ml.distance.*` : Distances
  - `org.apache.commons.math3.stat.*` : Statistiques

### 2. **Algorithmes ML**
- **K-Nearest Neighbors (KNN)** : Recommandation basée sur similarité
- **Régression Linéaire** : Prédiction de charge
- **Clustering (K-Means)** : Regroupement de préférences

### 3. **Métriques**
- **Distance Euclidienne** : Similarité entre séances
- **Score Pondéré** : Évaluation multi-critères
- **Historique** : Données passées pour apprentissage

---

## 🔧 Architecture du Système

### 1. **Schéma de Base de Données**

```sql
-- Table pour stocker les préférences de salle
CREATE TABLE salle_preferences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    salle_id INT NOT NULL,
    preference_score DECIMAL(5,2) DEFAULT 0.00,
    last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (salle_id) REFERENCES salle(id),
    UNIQUE KEY unique_user_salle (user_id, salle_id)
);

-- Table pour les logs d'optimisation
CREATE TABLE optimization_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seance_id INT NOT NULL,
    suggested_salle_id INT NOT NULL,
    algorithm_used VARCHAR(50),
    confidence_score DECIMAL(5,2),
    accepted BOOLEAN DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seance_id) REFERENCES seance(id),
    FOREIGN KEY (suggested_salle_id) REFERENCES salle(id)
);

-- Table pour les statistiques d'utilisation des salles
CREATE TABLE salle_usage_stats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    salle_id INT NOT NULL,
    day_of_week INT NOT NULL,
    time_slot INT NOT NULL,
    usage_count INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (salle_id) REFERENCES salle(id),
    UNIQUE KEY unique_salle_day_time (salle_id, day_of_week, time_slot)
);
```

---

### 2. **Flux de Traitement**

```
Demande de Séance (Matière, Capacité, Équipement)
    ↓
Collecte de Données (Salles, Disponibilités, Préférences)
    ↓
Prétraitement (Normalisation, Filtrage)
    ↓
Algorithme IA (Scoring / Prédiction)
    ↓
Ranking des Salles
    ↓
Suggestion Top-N
    ↓
Feedback et Apprentissage
```

---

## 💻 Implémentation Technique

### 1. **Classe de Configuration IA**

```java
/**
 * Configuration pour l'optimisation des salles
 */
public class SalleOptimizationConfig {
    
    // Poids pour chaque critère (doit totaliser 1.0)
    public static final double WEIGHT_CAPACITY = 0.30;        // 30% - Adéquation capacité
    public static final double WEIGHT_AVAILABILITY = 0.25;    // 25% - Disponibilité
    public static final double WEIGHT_PREFERENCES = 0.20;      // 20% - Préférences historiques
    public static final double WEIGHT_EQUIPMENT = 0.15;        // 15% - Équipement
    public static final double WEIGHT_PROXIMITY = 0.10;        // 10% - Proximité
    
    // Seuils de confiance
    public static final double MIN_CONFIDENCE_SCORE = 0.60;   // Score minimum pour suggestion
    public static final double HIGH_CONFIDENCE_SCORE = 0.85;    // Score haute confiance
    
    // Paramètres KNN
    public static final int KNN_K_VALUE = 5;                 // 5 voisins les plus proches
    
    // Paramètres de temps réel
    public static final long FRESHNESS_THRESHOLD_MS = 
        7 * 24 * 60 * 60 * 1000;  // 7 jours en millisecondes
}
```

---

### 2. **Modèle de Données pour Scoring**

```java
/**
 * Représente une salle avec son score d'optimisation
 */
public class SalleScore {
    
    private Salle salle;
    private double capacityScore;        // Score d'adéquation capacité
    private double availabilityScore;    // Score de disponibilité
    private double preferenceScore;     // Score de préférence utilisateur
    private double equipmentScore;       // Score d'équipement
    private double proximityScore;       // Score de proximité
    private double totalScore;         // Score total pondéré
    
    public SalleScore(Salle salle) {
        this.salle = salle;
        this.capacityScore = 0.0;
        this.availabilityScore = 0.0;
        this.preferenceScore = 0.0;
        this.equipmentScore = 0.0;
        this.proximityScore = 0.0;
        this.totalScore = 0.0;
    }
    
    /**
     * Calcule le score total pondéré
     */
    public void calculateTotalScore() {
        
        this.totalScore = 
            SalleOptimizationConfig.WEIGHT_CAPACITY * capacityScore +
            SalleOptimizationConfig.WEIGHT_AVAILABILITY * availabilityScore +
            SalleOptimizationConfig.WEIGHT_PREFERENCES * preferenceScore +
            SalleOptimizationConfig.WEIGHT_EQUIPMENT * equipmentScore +
            SalleOptimizationConfig.WEIGHT_PROXIMITY * proximityScore;
    }
    
    // Getters
    public Salle getSalle() { return salle; }
    public double getTotalScore() { return totalScore; }
    
    @Override
    public String toString() {
        return String.format("%s - Score: %.2f", salle.getName(), totalScore);
    }
}
```

---

### 3. **Service Principal d'Optimisation**

```java
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.FastVector;

/**
 * Service IA pour l'optimisation des salles
 */
public class SalleOptimizationService {
    
    private SalleService salleService;
    private SeanceService seanceService;
    private SallePreferenceService preferenceService;
    private SalleUsageStatsService statsService;
    
    public SalleOptimizationService() {
        this.salleService = new SalleService();
        this.seanceService = new SeanceService();
        this.preferenceService = new SallePreferenceService();
        this.statsService = new SalleUsageStatsService();
    }
    
    /**
     * Suggère les meilleures salles pour une séance
     * 
     * @param request Requête d'optimisation
     * @return Liste des salles suggérées avec scores
     */
    public List<SalleScore> suggestSalles(OptimizationRequest request) {
        
        // 1. Récupérer toutes les salles disponibles
        List<Salle> allSalles;
        try {
            allSalles = salleService.getAllSalles();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        
        // 2. Filtrer par capacité minimale
        List<Salle> capacityFiltered = allSalles.stream()
            .filter(s -> s.getCapacite() >= request.getRequiredCapacity())
            .toList();
        
        if (capacityFiltered.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 3. Calculer les scores pour chaque salle
        List<SalleScore> scoredSalles = new ArrayList<>();
        
        for (Salle salle : capacityFiltered) {
            
            SalleScore score = new SalleScore(salle);
            
            // Calculer les sous-scores
            score.setCapacityScore(
                calculateCapacityScore(salle, request.getRequiredCapacity())
            );
            
            score.setAvailabilityScore(
                calculateAvailabilityScore(salle, request.getStartTime(), request.getEndTime())
            );
            
            score.setPreferenceScore(
                calculatePreferenceScore(salle, request.getUserId())
            );
            
            score.setEquipmentScore(
                calculateEquipmentScore(salle, request.getRequiredEquipment())
            );
            
            score.setProximityScore(
                calculateProximityScore(salle, request.getUserLocation())
            );
            
            // Score total
            score.calculateTotalScore();
            
            scoredSalles.add(score);
        }
        
        // 4. Filtrer par score minimum
        List<SalleScore> filteredScores = scoredSalles.stream()
            .filter(s -> s.getTotalScore() >= SalleOptimizationConfig.MIN_CONFIDENCE_SCORE)
            .toList();
        
        // 5. Trier par score décroissant
        filteredScores.sort((s1, s2) -> 
            Double.compare(s2.getTotalScore(), s1.getTotalScore())
        );
        
        // 6. Retourner les Top-N (ex: Top-5)
        return filteredScores.stream()
            .limit(request.getMaxSuggestions())
            .toList();
    }
}
```

---

### 4. **Calcul des Scores**

#### 4.1 Score de Capacité

```java
/**
 * Calcule le score d'adéquation de capacité
 * 
 * Idée : Une salle avec capacité exactement requise = 1.0
 * Une salle avec capacité légèrement supérieure = 0.9
 * Une salle avec capacité très supérieure = 0.5 (gaspillage)
 */
private double calculateCapacityScore(Salle salle, int requiredCapacity) {
    
    int salleCapacity = salle.getCapacite();
    
    if (salleCapacity < requiredCapacity) {
        return 0.0;  // Insuffisant
    }
    
    // Ratio de remplissage
    double fillRatio = (double) requiredCapacity / salleCapacity;
    
    // Score basé sur le ratio
    // 1.0 = parfait, diminue si surdimensionné
    if (fillRatio >= 0.8 && fillRatio <= 1.0) {
        return 1.0;  // Capacité idéale (80-100% rempli)
    } else if (fillRatio >= 0.6 && fillRatio < 0.8) {
        return 0.9;  // Très bon
    } else if (fillRatio >= 0.4 && fillRatio < 0.6) {
        return 0.7;  // Bon
    } else if (fillRatio >= 0.2 && fillRatio < 0.4) {
        return 0.5;  // Acceptable
    } else {
        return 0.3;  // Salle trop grande
    }
}
```

#### 4.2 Score de Disponibilité

```java
/**
 * Calcule le score de disponibilité
 * 
 * Idée : Salle libre autour de l'horaire souhaité = score élevé
 */
private double calculateAvailabilityScore(Salle salle, 
                                     LocalDateTime start, LocalDateTime end) {
    
    try {
        List<Seance> salleSeances = seanceService
            .getSeancesBySalle(salle.getId());
        
        // Vérifier les créneaux adjacents (±1h)
        LocalDateTime windowStart = start.minusHours(1);
        LocalDateTime windowEnd = end.plusHours(1);
        
        int conflictingSeances = 0;
        
        for (Seance s : salleSeances) {
            if (s.getHeureDebut() == null || s.getHeureFin() == null) continue;
            
            LocalDateTime sStart = s.getHeureDebut().toLocalDateTime();
            LocalDateTime sEnd = s.getHeureFin().toLocalDateTime();
            
            // Vérifier le chevauchement
            if (windowStart.isBefore(sEnd) && windowEnd.isAfter(sStart)) {
                conflictingSeances++;
            }
        }
        
        // Score basé sur le nombre de conflits
        if (conflictingSeances == 0) {
            return 1.0;  // Parfaitement libre
        } else if (conflictingSeances == 1) {
            return 0.7;  // Un conflit proche
        } else if (conflictingSeances == 2) {
            return 0.4;  // Quelques conflits
        } else {
            return 0.1;  // Très occupée
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
        return 0.5;  // Score neutre en cas d'erreur
    }
}
```

#### 4.3 Score de Préférences Historiques

```java
/**
 * Calcule le score basé sur les préférences utilisateur
 * 
 * Idée : Si l'utilisateur a souvent utilisé cette salle → score élevé
 */
private double calculatePreferenceScore(Salle salle, int userId) {
    
    try {
        // Récupérer la préférence pour cette salle
        SallePreference preference = preferenceService
            .getPreference(userId, salle.getId());
        
        if (preference == null) {
            return 0.5;  // Préférence neutre
        }
        
        // Score basé sur le score de préférence (0.0 à 1.0)
        return preference.getScore();
        
    } catch (SQLException e) {
        e.printStackTrace();
        return 0.5;
    }
}
```

#### 4.4 Score d'Équipement

```java
/**
 * Calcule le score d'équipement
 * 
 * Idée : La salle a-t-elle l'équipement requis ?
 */
private double calculateEquipmentScore(Salle salle, List<String> requiredEquipment) {
    
    if (requiredEquipment == null || requiredEquipment.isEmpty()) {
        return 1.0;  // Pas d'équipement requis
    }
    
    // Récupérer l'équipement de la salle (hypothétique)
    List<String> salleEquipment = getEquipmentForSalle(salle.getId());
    
    // Calculer le pourcentage d'équipement présent
    int matched = 0;
    for (String required : requiredEquipment) {
        if (salleEquipment.contains(required)) {
            matched++;
        }
    }
    
    if (requiredEquipment.isEmpty()) {
        return 1.0;
    }
    
    return (double) matched / requiredEquipment.size();
}
```

#### 4.5 Score de Proximité

```java
/**
 * Calcule le score de proximité géographique
 * 
 * Idée : Salle proche de la position utilisateur = score élevé
 */
private double calculateProximityScore(Salle salle, GeoLocation userLocation) {
    
    if (userLocation == null) {
        return 0.5;  // Position inconnue → score neutre
    }
    
    // Coordonnées de la salle (hypothétique)
    GeoLocation salleLocation = getSalleLocation(salle.getId());
    
    // Calculer la distance en mètres
    double distance = calculateDistance(userLocation, salleLocation);
    
    // Score basé sur la distance
    // < 50m = 1.0, > 500m = 0.1
    if (distance < 50) {
        return 1.0;
    } else if (distance < 100) {
        return 0.9;
    } else if (distance < 200) {
        return 0.7;
    } else if (distance < 300) {
        return 0.5;
    } else if (distance < 500) {
        return 0.3;
    } else {
        return 0.1;
    }
}

/**
 * Calcule la distance entre deux points géographiques (Haversine)
 */
private double calculateDistance(GeoLocation loc1, GeoLocation loc2) {
    
    final int R = 6371000;  // Rayon de la Terre en mètres
    
    double lat1 = Math.toRadians(loc1.getLatitude());
    double lon1 = Math.toRadians(loc1.getLongitude());
    double lat2 = Math.toRadians(loc2.getLatitude());
    double lon2 = Math.toRadians(loc2.getLongitude());
    
    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;
    
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1) * Math.cos(lat2) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    
    return R * c;  // Distance en mètres
}
```

---

### 5. **Algorithme KNN pour Recommandation**

```java
import weka.classifiers.lazy.IBk;
import weka.core.*;
import java.util.*;

/**
 * Recommandation basée sur K-Nearest Neighbors
 */
public class KNNRecommender {
    
    private IBk knnClassifier;
    private Instances trainingData;
    
    public KNNRecommender() throws Exception {
        
        // 1. Créer les attributs
        FastVector<Attribute> attributes = new FastVector<>();
        attributes.addElement(new Attribute("dayOfWeek", Arrays.asList("LUN", "MAR", "MER", "JEU", "VEN", "SAM")));
        attributes.addElement(new Attribute("hour"));
        attributes.addElement(new Attribute("duration"));
        attributes.addElement(new Attribute("capacity"));
        attributes.addElement(new Attribute("salleId"));
        
        // 2. Créer les instances
        trainingData = new Instances("HistoricalData", attributes, 0);
        trainingData.setClassIndex(4);  // salleId comme classe
        
        // 3. Charger les données historiques
        loadHistoricalData();
        
        // 4. Initialiser KNN
        knnClassifier = new IBk();
        knnClassifier.setKNN(SalleOptimizationConfig.KNN_K_VALUE);
        knnClassifier.buildClassifier(trainingData);
    }
    
    /**
     * Charge les données historiques depuis la base de données
     */
    private void loadHistoricalData() {
        
        try {
            List<Seance> historicalSeances = seanceService.getAllSeances();
            
            for (Seance seance : historicalSeances) {
                if (seance.getHeureDebut() == null || seance.getHeureFin() == null) {
                    continue;
                }
                
                Instance instance = createInstanceFromSeance(seance);
                trainingData.add(instance);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crée une instance Weka à partir d'une séance
     */
    private Instance createInstanceFromSeance(Seance seance) {
        
        Instance instance = new Instance(5);
        
        // Jour de la semaine
        String dayLetter = getDayLetter(seance.getJour());
        instance.setValue(0, dayLetter);
        
        // Heure de début
        instance.setValue(1, seance.getHeureDebut().toLocalDateTime().getHour());
        
        // Durée en heures
        long durationMs = seance.getHeureFin().getTime() - 
                         seance.getHeureDebut().getTime();
        double durationHours = durationMs / (1000.0 * 60 * 60);
        instance.setValue(2, durationHours);
        
        // Capacité
        Salle salle = salleService.getById(seance.getSalleId());
        instance.setValue(3, salle.getCapacite());
        
        // Salle ID (classe)
        instance.setValue(4, seance.getSalleId());
        
        instance.setDataset(trainingData);
        return instance;
    }
    
    /**
     * Recommande une salle pour une nouvelle séance
     */
    public List<Integer> recommendSalles(Seance newSeance, int topN) throws Exception {
        
        // Créer l'instance de requête
        Instance query = createInstanceFromSeance(newSeance);
        query.setClassMissing();  // Classe non définie
        
        // Trouver les k plus proches voisins
        Instance[] neighbors = knnClassifier.kNearestNeighbours(query, topN);
        
        // Extraire les IDs de salle recommandés
        List<Integer> recommendedSalleIds = new ArrayList<>();
        for (Instance neighbor : neighbors) {
            int salleId = (int) neighbor.classValue();
            recommendedSalleIds.add(salleId);
        }
        
        return recommendedSalleIds;
    }
    
    /**
     * Convertit le nom du jour en lettre
     */
    private String getDayLetter(String dayName) {
        
        return switch (dayName.toLowerCase()) {
            case "lundi" -> "LUN";
            case "mardi" -> "MAR";
            case "mercredi" -> "MER";
            case "jeudi" -> "JEU";
            case "vendredi" -> "VEN";
            case "samedi" -> "SAM";
            default -> "LUN";
        };
    }
}
```

---

### 6. **Apprentissage par Feedback**

```java
/**
 * Met à jour les préférences utilisateur après feedback
 */
public void updatePreferences(int userId, int salleId, boolean wasAccepted) {
    
    try {
        // Récupérer ou créer la préférence
        SallePreference preference = preferenceService.getPreference(userId, salleId);
        
        if (preference == null) {
            preference = new SallePreference();
            preference.setUserId(userId);
            preference.setSalleId(salleId);
            preference.setScore(0.5);  // Score neutre initial
        }
        
        // Ajuster le score basé sur le feedback
        double adjustment = wasAccepted ? 0.1 : -0.05;
        double newScore = preference.getScore() + adjustment;
        
        // Limiter entre 0 et 1
        newScore = Math.max(0.0, Math.min(1.0, newScore));
        
        preference.setScore(newScore);
        preference.setLastUsed(new Timestamp(System.currentTimeMillis()));
        
        // Sauvegarder
        preferenceService.save(preference);
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

---

### 7. **Contrôleur JavaFX pour Interface**

```java
import javafx.scene.control.ListView;
import javafx.scene.control.Button;

/**
 * Contrôleur pour l'interface d'optimisation IA
 */
public class SalleOptimizationController {
    
    @FXML private ComboBox<Integer> comboCapacite;
    @FXML private CheckBox cbVideoProjecteur;
    @FXML private CheckBox cbTableauBlanc;
    @FXML private Button btnOptimiser;
    @FXML private ListView<SalleScore> lstSuggestions;
    
    private SalleOptimizationService optimizationService;
    
    public void initialize() {
        optimizationService = new SalleOptimizationService();
    }
    
    /**
     * Lance l'optimisation IA
     */
    @FXML
    private void handleOptimiser() {
        
        int requiredCapacity = comboCapacite.getValue();
        if (requiredCapacity == 0) {
            showAlert("Erreur", "Veuillez sélectionner une capacité.");
            return;
        }
        
        // Créer la requête
        OptimizationRequest request = new OptimizationRequest();
        request.setUserId(SessionManager.getInstance().getCurrentUser().getId());
        request.setRequiredCapacity(requiredCapacity);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setRequiredEquipment(getSelectedEquipment());
        request.setMaxSuggestions(5);
        
        // Exécuter l'optimisation
        List<SalleScore> suggestions = optimizationService.suggestSalles(request);
        
        // Afficher les résultats
        displaySuggestions(suggestions);
    }
    
    /**
     * Affiche les suggestions dans la liste
     */
    private void displaySuggestions(List<SalleScore> suggestions) {
        
        lstSuggestions.getItems().clear();
        
        if (suggestions.isEmpty()) {
            showAlert("Information", "Aucune salle ne correspond aux critères.");
            return;
        }
        
        lstSuggestions.getItems().addAll(suggestions);
    }
    
    /**
     * Récupère l'équipement sélectionné
     */
    private List<String> getSelectedEquipment() {
        
        List<String> equipment = new ArrayList<>();
        
        if (cbVideoProjecteur.isSelected()) {
            equipment.add("VIDEO_PROJECTEUR");
        }
        if (cbTableauBlanc.isSelected()) {
            equipment.add("TABLEAU_BLANC");
        }
        
        return equipment;
    }
}
```

---

## 📊 Analyses et Rapports

### 1. **Taux d'Acceptation des Suggestions**

```java
/**
 * Calcule le taux d'acceptation des suggestions IA
 */
public double calculateAcceptanceRate() {
    
    String sql = "SELECT COUNT(*) as total, " +
                 "SUM(CASE WHEN accepted = 1 THEN 1 ELSE 0 END) as accepted " +
                 "FROM optimization_logs";
    
    try (Statement st = connection.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        
        if (rs.next()) {
            int total = rs.getInt("total");
            int accepted = rs.getInt("accepted");
            
            if (total == 0) return 0.0;
            
            return (double) accepted / total * 100;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return 0.0;
}
```

---

## 🚀 Optimisations

### 1. **Cache des Scores**

```java
private Map<String, List<SalleScore>> scoreCache = new HashMap<>();

/**
 * Récupère les suggestions depuis le cache si disponibles
 */
public List<SalleScore> getCachedSuggestions(OptimizationRequest request) {
    
    // Clé de cache basée sur les paramètres de requête
    String cacheKey = request.getCacheKey();
    
    if (scoreCache.containsKey(cacheKey)) {
        return scoreCache.get(cacheKey);
    }
    
    // Calculer et mettre en cache
    List<SalleScore> suggestions = suggestSalles(request);
    scoreCache.put(cacheKey, suggestions);
    
    return suggestions;
}
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **Framework ML** | Weka (Waikato Environment) |
| **Algorithmes** | KNN, Régression, Clustering |
| **Critères** | Capacité, Disponibilité, Préférences, Équipement, Proximité |
| **Scoring** | Pondéré (total = 1.0) |
| **Apprentissage** | Feedback utilisateur |
| **Calcul Distance** | Haversine (géographique) |
| **Cache** | ScoreCache pour performances |
| **Confidence** | Score minimum 0.60 |

---

## 🔗 Ressources

- **Weka Documentation** : https://www.cs.waikato.ac.nz/ml/weka/
- **Machine Learning Java** : Various ML Libraries
- **Recommendation Systems** : Collaborative Filtering

---

**Documentation générée pour EspritFlow - Optimisation des Salles par IA** 🤖
