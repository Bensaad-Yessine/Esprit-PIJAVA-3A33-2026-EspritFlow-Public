# 📂 Fichiers Statistiques Front Office - Localisation Complète

## 🎯 Vue d'ensemble
Cette fonctionnalité affiche des statistiques visuelles dans le dashboard frontoffice pour aider les étudiants à suivre leur progression.

---

## 📁 Structure des Fichiers

### 1️⃣ Contrôleurs (Controllers)

#### FrontDashboardController.java
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/FrontDashboardController.java`

**Responsabilité** : Gestion du dashboard et des graphiques statistiques

**Méthodes clés** :

```java
// Ligne 30-60 : Déclarations des composants graphiques
@FXML
private BarChart<String, Number> weeklyAttendanceChart;
@FXML
private PieChart courseDistributionPieChart;
@FXML
private Label totalSeancesLabel;
@FXML
private Label attendanceRateLabel;
@FXML
private Label nextSeanceLabel;
@FXML
private Label averageGradeLabel;

// Ligne 70-100 : Initialisation
@Override
public void initialize(URL location, ResourceBundle resources) {
    loadStatistics();
    updateCharts();
    loadNextSeance();
}

// Ligne 105-130 : Chargement des statistiques
private void loadStatistics() {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    
    // Récupération des données depuis le service
    int totalSeances = seanceService.countSeancesByUser(currentUser.getId());
    int attendedSeances = attendanceService.countAttendedByUser(currentUser.getId());
    double attendanceRate = (attendedSeances * 100.0) / totalSeances;
    
    // Mise à jour des labels
    totalSeancesLabel.setText(String.valueOf(totalSeances));
    attendanceRateLabel.setText(String.format("%.1f%%", attendanceRate));
}

// Ligne 135-170 : Mise à jour des graphiques
private void updateCharts() {
    updateWeeklyAttendanceChart();
    updateCourseDistributionChart();
}

// Ligne 175-210 : Graphique de présence hebdomadaire
private void updateWeeklyAttendanceChart() {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    List<Attendance> attendances = attendanceService.getWeeklyAttendances(currentUser.getId());
    
    // Préparation des données pour le graphique
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Présences");
    
    String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    for (String day : days) {
        int count = attendanceService.countAttendancesByDay(currentUser.getId(), day);
        series.getData().add(new XYChart.Data<>(day, count));
    }
    
    weeklyAttendanceChart.getData().add(series);
}

// Ligne 215-250 : Graphique de distribution des cours
private void updateCourseDistributionChart() {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    List<Seance> seances = seanceService.getAllSeances();
    
    // Comptage par matière
    Map<Integer, Integer> matiereCount = new HashMap<>();
    for (Seance s : seances) {
        if (s.getClasseId().equals(currentUser.getClasse_id())) {
            matiereCount.merge(s.getMatiereId(), 1, Integer::sum);
        }
    }
    
    // Création du PieChart
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    for (Map.Entry<Integer, Integer> entry : matiereCount.entrySet()) {
        String matiereName = matiereService.getById(entry.getKey()).getNom();
        pieChartData.add(new PieChart.Data(matiereName, entry.getValue()));
    }
    
    courseDistributionPieChart.setData(pieChartData);
}

// Ligne 255-280 : Chargement de la prochaine séance
private void loadNextSeance() {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    Seance nextSeance = seanceService.getNextSeance(currentUser.getId());
    
    if (nextSeance != null) {
        String matiereName = matiereService.getById(nextSeance.getMatiereId()).getNom();
        nextSeanceLabel.setText(matiereName + " - " + nextSeance.getJour());
    } else {
        nextSeanceLabel.setText("Aucune séance à venir");
    }
}
```

**Où trouver les données** :
- Séances : `src/main/java/piJava/services/SeanceService.java`
- Présences : `src/main/java/piJava/services/AttendanceService.java`
- Matières : `src/main/java/piJava/services/MatiereService.java`
- Session utilisateur : `SessionManager.getInstance().getCurrentUser()`

---

### 2️⃣ Services (Business Logic)

#### SeanceService.java
**Emplacement** : `src/main/java/piJava/services/SeanceService.java`

**Méthodes utilisées pour les statistiques** :

```java
// Ligne 150-170 : Compter les séances d'un utilisateur
public int countSeancesByUser(int userId) {
    try {
        user u = userServices.getById(userId);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM seance WHERE classe_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, u.getClasse_id());
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return 0;
    }
}

// Ligne 175-200 : Récupérer la prochaine séance
public Seance getNextSeance(int userId) {
    try {
        user u = userServices.getById(userId);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM seance WHERE classe_id = ? AND heure_debut > ? ORDER BY heure_debut ASC LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, u.getClasse_id());
        stmt.setTimestamp(2, now);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Seance s = new Seance();
            // Mapping des champs
            return s;
        }
        return null;
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}
```

**Où trouver les données** :
- Table BDD : `seance`
- Colonnes utilisées : `id`, `classe_id`, `matiere_id`, `heure_debut`, `jour`

---

#### AttendanceService.java
**Emplacement** : `src/main/java/piJava/services/AttendanceService.java`

**Méthodes utilisées pour les statistiques** :

```java
// Ligne 100-125 : Compter les présences d'un utilisateur
public int countAttendedByUser(int userId) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM attendance WHERE user_id = ? AND status = 'PRESENT'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return 0;
    }
}

// Ligne 130-160 : Récupérer les présences hebdomadaires
public List<Attendance> getWeeklyAttendances(int userId) {
    List<Attendance> list = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        // Calcul du début de semaine
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Timestamp startOfWeek = new Timestamp(cal.getTimeInMillis());
        
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND timestamp >= ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setTimestamp(2, startOfWeek);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Attendance a = new Attendance();
            // Mapping des champs
            list.add(a);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

// Ligne 165-195 : Compter les présences par jour
public int countAttendancesByDay(int userId, String day) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM attendance a " +
                     "JOIN seance s ON a.seance_id = s.id " +
                     "WHERE a.user_id = ? AND s.jour = ? AND a.status = 'PRESENT'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setString(2, day);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return 0;
    }
}
```

**Où trouver les données** :
- Table BDD : `attendance`
- Colonnes utilisées : `id`, `user_id`, `seance_id`, `timestamp`, `status`

---

#### MatiereService.java
**Emplacement** : `src/main/java/piJava/services/MatiereService.java`

**Méthodes utilisées pour les statistiques** :

```java
// Ligne 50-75 : Récupérer une matière par ID
public Matiere getById(int id) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM matiere WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Matiere m = new Matiere();
            m.setId(rs.getInt("id"));
            m.setNom(rs.getString("nom"));
            m.setCode(rs.getString("code"));
            return m;
        }
        return null;
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}
```

**Où trouver les données** :
- Table BDD : `matiere`
- Colonnes utilisées : `id`, `nom`, `code`

---

### 3️⃣ Entités (Data Models)

#### Seance.java
**Emplacement** : `src/main/java/piJava/entities/Seance.java`

**Attributs utilisés pour les statistiques** :

```java
// Ligne 15-40
private Integer id;
private Integer classeId;      // Pour filtrer par classe de l'utilisateur
private Integer matiereId;      // Pour grouper par matière
private String jour;            // Pour les statistiques par jour
private Timestamp heureDebut;   // Pour trouver la prochaine séance
private Timestamp heureFin;
private String typeSeance;
```

---

#### Attendance.java
**Emplacement** : `src/main/java/piJava/entities/Attendance.java`

**Attributs utilisés pour les statistiques** :

```java
// Ligne 10-30
private Integer id;
private Integer seanceId;       // Pour rejoindre avec la table seance
private Integer userId;         // Pour filtrer par utilisateur
private Timestamp timestamp;    // Pour le filtrage hebdomadaire
private String status;          // PRESENT, ABSENT, LATE
```

---

#### Matiere.java
**Emplacement** : `src/main/java/piJava/entities/Matiere.java`

**Attributs utilisés pour les statistiques** :

```java
// Ligne 15-30
private Integer id;
private String nom;            // Affiché dans le PieChart
private String code;
// ...
```

---

### 4️⃣ Fichiers FXML (UI Views)

#### dashboard-content.fxml
**Emplacement** : `src/main/resources/frontoffice/dashboard/dashboard-content.fxml`

**Structure des composants graphiques** :

```xml
<!-- Ligne 10-30 : Conteneur principal -->
<VBox spacing="20" styleClass="dashboard-container">
    
    <!-- Ligne 35-55 : Carte KPI -->
    <HBox spacing="20" styleClass="kpi-container">
        <VBox spacing="5" styleClass="kpi-card">
            <Label text="Total Séances" styleClass="kpi-label"/>
            <Label fx:id="totalSeancesLabel" text="0" styleClass="kpi-value"/>
        </VBox>
        
        <VBox spacing="5" styleClass="kpi-card">
            <Label text="Taux de Présence" styleClass="kpi-label"/>
            <Label fx:id="attendanceRateLabel" text="0%" styleClass="kpi-value"/>
        </VBox>
        
        <VBox spacing="5" styleClass="kpi-card">
            <Label text="Prochaine Séance" styleClass="kpi-label"/>
            <Label fx:id="nextSeanceLabel" text="-" styleClass="kpi-value-small"/>
        </VBox>
    </HBox>
    
    <!-- Ligne 60-80 : Graphiques -->
    <HBox spacing="20">
        <VBox spacing="10" styleClass="chart-card">
            <Label text="Présences Hebdomadaires" styleClass="chart-title"/>
            <BarChart fx:id="weeklyAttendanceChart"/>
        </VBox>
        
        <VBox spacing="10" styleClass="chart-card">
            <Label text="Distribution des Cours" styleClass="chart-title"/>
            <PieChart fx:id="courseDistributionPieChart"/>
        </VBox>
    </HBox>
</VBox>
```

**Où trouver les données** :
- Les `fx:id` correspondent aux variables du contrôleur Java
- Les données sont injectées via le contrôleur `FrontDashboardController`

---

### 5️⃣ Styles CSS

#### dashboard.css
**Emplacement** : `src/main/resources/frontoffice/dashboard/dashboard.css`

**Styles pour les KPIs** :

```css
/* Ligne 15-30 : KPI Cards */
.kpi-container {
    -fx-padding: 20;
    -fx-spacing: 20;
}

.kpi-card {
    -fx-background-color: white;
    -fx-background-radius: 10;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
    -fx-padding: 15;
    -fx-min-width: 150;
    -fx-alignment: center;
}

.kpi-label {
    -fx-font-size: 12px;
    -fx-text-fill: #666;
}

.kpi-value {
    -fx-font-size: 28px;
    -fx-font-weight: bold;
    -fx-text-fill: #2563eb;
}

.kpi-value-small {
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-text-fill: #2563eb;
}

/* Ligne 35-60 : Charts */
.chart-card {
    -fx-background-color: white;
    -fx-background-radius: 10;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
    -fx-padding: 15;
}

.chart-title {
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-text-fill: #333;
}
```

---

## 📊 Flux de Données

### Chargement du Dashboard
```
[FrontDashboardController.initialize()]
        ↓
[SessionManager.getCurrentUser()]
        ↓
[SeanceService] → [Table seance]
        ↓
[AttendanceService] → [Table attendance]
        ↓
[MatiereService] → [Table matiere]
        ↓
[Calcul des KPIs]
        ↓
[Mise à jour des Labels et Charts]
```

---

## 🔍 Comment Trouver les Données

### Pour modifier les KPIs affichés :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/FrontDashboardController.java`
2. **Méthode** : `loadStatistics()` (lignes 105-130)
3. **Données** : Ajouter des appels aux services appropriés
4. **Affichage** : Modifier les labels dans `dashboard-content.fxml`

### Pour ajouter un nouveau graphique :
1. **FXML** : Ajouter un composant Chart dans `dashboard-content.fxml`
2. **Contrôleur** : Déclarer le composant avec `@FXML`
3. **Méthode** : Créer une méthode `updateNewChart()` dans `FrontDashboardController`
4. **Données** : Ajouter une méthode dans le Service approprié

### Pour modifier les styles :
1. **Fichier CSS** : `src/main/resources/frontoffice/dashboard/dashboard.css`
2. **Composants** : Ajouter ou modifier les classes CSS
3. **FXML** : Appliquer les classes avec `styleClass`

---

## 📈 Métriques Calculées

### 1. Taux de Présence
```java
double attendanceRate = (attendedSeances * 100.0) / totalSeances;
```
- **Source** : `AttendanceService.countAttendedByUser()` / `SeanceService.countSeancesByUser()`
- **Affichage** : Label `attendanceRateLabel`

### 2. Présences Hebdomadaires
```java
int count = attendanceService.countAttendancesByDay(userId, day);
```
- **Source** : `AttendanceService.countAttendancesByDay()`
- **Affichage** : BarChart `weeklyAttendanceChart`

### 3. Distribution des Cours
```java
matiereCount.merge(s.getMatiereId(), 1, Integer::sum);
```
- **Source** : `SeanceService.getAllSeances()` + `MatiereService.getById()`
- **Affichage** : PieChart `courseDistributionPieChart`

---

## 🚀 Points d'Entrée

### Frontoffice
- **Menu Dashboard** → Affichage automatique des statistiques
- **Chargement** : Automatique à l'ouverture du dashboard (`initialize()`)
- **Mise à jour** : Recharger la vue pour rafraîchir les données

---

**Dernière mise à jour** : 30 avril 2026
