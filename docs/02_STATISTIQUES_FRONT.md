# 📊 Documentation Fonctionnalité : Statistiques Front Office

## 📋 Vue d'ensemble

La fonctionnalité de statistiques en Front Office permet aux étudiants de visualiser leur emploi du temps de manière interactive avec des graphiques, des KPIs (Indicateurs Clés de Performance), et des analyses de charge de travail.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **JavaFX**
- **Graphismes** : BarCharts, PieCharts
- **UI Components** : VBox, HBox, GridPane
- **Animations** : FadeTransition
- **Styling** : CSS personnalisé

### 2. **Bibliothèques Java**
- **Java 8+ Stream API** : Traitement fonctionnel des données
- **Java Time API** : Gestion des dates et heures
- **iText** : Export PDF
- **SwingFXUtils** : Capture d'écran UI

### 3. **Données**
- **JDBC** : Accès à la base de données
- **Services Layer** : Séparation des métiers
- **Entities** : POJOs (Plain Old Java Objects)

---

## 🔧 Architecture du Système

### 1. **Structure des Contrôleurs**

```
EmploiContentController
├── SeanceService (Data Access)
├── MatiereService (Matières)
├── ClasseService (Classes)
└── SalleService (Salles)
```

### 2. **Flux de données pour les statistiques**

```
Base de Données
    ↓
SeanceService.getAllSeances()
    ↓
Filtrage par classe
    ↓
Agrégation par jour/semaine
    ↓
Calcul des KPIs
    ↓
Affichage dans l'interface
```

---

## 💻 Implémentation Technique

### 1. **KPIs (Key Performance Indicators)**

#### 1.1 Séances Totales

```java
/**
 * Compte le nombre total de séances pour la semaine courante
 */
@FXML private Label statTotalSeances;

private void updateKPIs() {
    
    // Récupérer toutes les séances de la semaine
    List<Seance> weekSeances = getSeancesForCurrentWeek();
    
    // Afficher le total
    statTotalSeances.setText(String.valueOf(weekSeances.size()));
}
```

#### 1.2 Nombre de Matières

```java
@FXML private Label statMatieres;

private void updateKPIs() {
    
    // Compter les matières distinctes
    long matCount = seances.stream()
        .map(Seance::getMatiereId)           // Extraire les IDs de matière
        .distinct()                             // Éliminer les doublons
        .count();                              // Compter
    
    statMatieres.setText(String.valueOf(matCount));
}
```

#### 1.3 Nombre de Salles

```java
@FXML private Label statSalles;

private void updateKPIs() {
    
    // Compter les salles distinctes
    long salCount = seances.stream()
        .map(Seance::getSalleId)
        .distinct()
        .count();
    
    statSalles.setText(String.valueOf(salCount));
}
```

#### 1.4 Heures Totales

```java
@FXML private Label statHeures;

private void updateKPIs() {
    
    double totalHours = 0;
    
    for (Seance s : seances) {
        if (s.getHeureDebut() != null && s.getHeureFin() != null) {
            // Calcul de la durée en heures
            long diffMs = s.getHeureFin().getTime() - s.getHeureDebut().getTime();
            double hours = diffMs / (1000.0 * 60 * 60);
            
            totalHours += hours;
        }
    }
    
    // Formatage : afficher en heures entières si pas de décimales
    statHeures.setText(
        totalHours == (long) totalHours 
            ? String.format("%d", (long)totalHours)
            : String.format("%.1f", totalHours)
    );
}
```

---

### 2. **Analyse de Charge par Jour**

#### 2.1 Affichage des Séances par Jour

```java
@FXML private HBox chargeSemaineContainer;

private void updateChargeJour() {
    
    chargeSemaineContainer.getChildren().clear();
    
    int maxSeances = 0;
    double totalHoursWeek = 0;

    // Jours de la semaine (Lundi à Samedi)
    String[] jFull = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    
    for (int i = 0; i < jFull.length; i++) {
        String jourName = jFull[i];
        
        // Filtrer les séances pour ce jour
        List<Seance> sj = seances.stream()
            .filter(s -> s.getJour().equalsIgnoreCase(jourName))
            .toList();
        
        int count = sj.size();
        if (count > maxSeances) maxSeances = count;

        // Calculer les heures pour ce jour
        double hours = 0;
        for (Seance s : sj) {
            if (s.getHeureDebut() != null && s.getHeureFin() != null) {
                long diffMs = s.getHeureFin().getTime() - s.getHeureDebut().getTime();
                hours += diffMs / (1000.0 * 60 * 60);
            }
        }
        totalHoursWeek += hours;

        // Créer la box du jour
        VBox box = createDayBox(i, count, hours);
        chargeSemaineContainer.getChildren().add(box);
    }
    
    // Mettre à jour les statistiques globales
    lblMaxSeances.setText(String.valueOf(maxSeances));
    lblTotalHeuresStat.setText(formatHours(totalHoursWeek));
}
```

#### 2.2 Création des Boxes de Jour

```java
private VBox createDayBox(int dayIndex, int count, double hours) {
    
    String[] j = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
    
    VBox box = new VBox(5);
    box.getStyleClass().add("charge-day-box");
    
    // Styling conditionnel
    if (count > 0) {
        box.getStyleClass().add("active");  // Jour avec séances
    } else {
        box.getStyleClass().add("empty");   // Jour libre
    }

    // Label du jour (ex: Lun)
    Label l1 = new Label(j[dayIndex]);
    l1.getStyleClass().add("charge-day-label");
    
    // Nombre de séances (ex: 3)
    Label l2 = new Label(String.valueOf(count));
    l2.getStyleClass().add("charge-day-val");
    
    // Heures (ex: 4.5h)
    Label l3 = new Label(hours > 0 
        ? (hours == (long)hours ? (long)hours+"h" : String.format("%.1fh", hours))
        : "0h"
    );
    l3.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");

    box.getChildren().addAll(l1, l2, l3);
    return box;
}
```

---

### 3. **Graphique en Barres (BarChart)**

#### 3.1 Configuration du BarChart

```java
@FXML private BarChart<String, Number> barChartStats;

/**
 * Met à jour le graphique en barres avec les séances par jour
 */
private void updateBarChart() {
    
    // Créer une série de données
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    
    String[] jFull = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    String[] j = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
    String[] jCouleurs = {"#ef4444", "#f59e0b", "#10b981", "#3b82f6", "#8b5cf6", "#ec4899"};
    
    // Ajouter les données pour chaque jour
    for (int i = 0; i < jFull.length; i++) {
        final String fullDayName = jFull[i];
        final String shortDayName = j[i];
        
        // Compter les séances pour ce jour
        int count = (int) seances.stream()
            .filter(s -> s.getJour().equalsIgnoreCase(fullDayName))
            .count();
        
        // Créer le point de données
        XYChart.Data<String, Number> data = 
            new XYChart.Data<>(shortDayName, count);
        
        series.getData().add(data);
    }
    
    // Effacer les anciennes données
    barChartStats.getData().clear();
    
    // Ajouter la nouvelle série
    barChartStats.getData().add(series);
    
    // Appliquer les couleurs personnalisées
    applyCustomColors(series, jCouleurs);
}
```

#### 3.2 Application des Couleurs Personnalisées

```java
private void applyCustomColors(XYChart.Series<String, Number> series, String[] colors) {
    
    int index = 0;
    for (XYChart.Data<String, Number> data : series.getData()) {
        
        // Chaque barre a sa propre couleur
        String color = colors[index % colors.length];
        
        // Appliquer le style via CSS
        data.getNode().setStyle("-fx-bar-fill: " + color + ";");
        
        index++;
    }
}
```

**Tableau des couleurs par jour :**

| Jour | Couleur | Code Hex |
|------|---------|----------|
| Lundi | Rouge | `#ef4444` |
| Mardi | Orange | `#f59e0b` |
| Mercredi | Vert | `#10b981` |
| Jeudi | Bleu | `#3b82f6` |
| Vendredi | Violet | `#8b5cf6` |
| Samedi | Rose | `#ec4899` |

---

### 4. **Calendrier Hebdomadaire (GridPane)**

#### 4.1 Structure du GridPane

```java
@FXML private GridPane timetableGrid;

/**
 * Dessine le calendrier hebdomadaire
 */
private void drawTimetableGrid() {
    
    // Nettoyer le calendrier existant
    timetableGrid.getChildren().clear();
    timetableGrid.getColumnConstraints().clear();
    timetableGrid.getRowConstraints().clear();

    // Configuration des colonnes : Heure + 6 Jours
    setupColumns();
    
    // Configuration des lignes : Header + 20 blocs de 30min
    setupRows();
    
    // Dessiner le header
    drawHeader();
    
    // Dessiner le fond quadrillé
    drawBackgroundGrid();
    
    // Placer les séances
    drawSeances();
}
```

#### 4.2 Configuration des Colonnes

```java
private void setupColumns() {
    
    // Colonne Heure (petite)
    ColumnConstraints colHeure = new ColumnConstraints(60);
    timetableGrid.getColumnConstraints().add(colHeure);
    
    // Colonnes des jours (élargissables)
    for (int i = 0; i < 6; i++) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);  // Les colonnes s'élargissent
        cc.setMinWidth(100);
        timetableGrid.getColumnConstraints().add(cc);
    }
}
```

#### 4.3 Configuration des Lignes

```java
private void setupRows() {
    
    // Ligne Header (titres des jours)
    RowConstraints headerRow = new RowConstraints(40);
    timetableGrid.getRowConstraints().add(headerRow);
    
    // Lignes horaires (de 8h à 18h par pas de 30min)
    for (int i = 1; i <= 20; i++) {
        RowConstraints rc = new RowConstraints(30);  // 30px par bloc de 30min
        timetableGrid.getRowConstraints().add(rc);
    }
}
```

#### 4.4 Dessin des Séances

```java
private void drawSeances() {
    
    String[] jFull = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    String[] jCouleurs = {"#ef4444", "#f59e0b", "#10b981", "#3b82f6", "#8b5cf6", "#ec4899"};
    
    for (Seance s : seances) {
        if (s.getHeureDebut() == null || s.getHeureFin() == null) continue;
        
        // Déterminer la colonne (jour)
        int col = getColumnForDay(s.getJour(), jFull);
        if (col == 0) continue;
        
        // Calculer les positions dans la grille
        LocalDateTime start = s.getHeureDebut().toLocalDateTime();
        LocalDateTime end = s.getHeureFin().toLocalDateTime();
        
        int startH = start.getHour();
        int startM = start.getMinute();
        int endH = end.getHour();
        int endM = end.getMinute();
        
        // Conversion en ligne (chaque ligne = 15min)
        int rowStart = (startH - 8) * 4 + (startM / 15) + 1;
        int rowEnd = (endH - 8) * 4 + (endM / 15) + 1;
        int span = rowEnd - rowStart;
        
        if (span <= 0) span = 1;
        
        // Couleur du jour ou violet pour révision
        String color = "Révision".equalsIgnoreCase(s.getTypeSeance()) 
            ? "#8b5cf6" 
            : jCouleurs[col - 1];
        
        // Créer le bloc de séance
        VBox bloc = createSeanceBlock(s, color);
        
        // Ajouter à la grille
        GridPane.setMargin(bloc, new Insets(2, 4, 2, 4));
        timetableGrid.add(bloc, col, rowStart, 1, span);
    }
}
```

#### 4.5 Création du Bloc de Séance

```java
private VBox createSeanceBlock(Seance s, String color) {
    
    VBox bloc = new VBox(2);
    bloc.getStyleClass().add("seance-block");
    bloc.setStyle("-fx-background-color: " + color + ";");
    
    // Titre (Matière ou "Séance de révision")
    String titleText = "Révision".equalsIgnoreCase(s.getTypeSeance())
        ? "Séance de révision"
        : matiereMap.getOrDefault(s.getMatiereId(), "Inconnu");
    
    Label mName = new Label(titleText);
    mName.getStyleClass().add("seance-title");
    
    // Heure
    LocalDateTime start = s.getHeureDebut().toLocalDateTime();
    LocalDateTime end = s.getHeureFin().toLocalDateTime();
    String strTime = String.format("%02d:%02d - %02d:%02d", 
        start.getHour(), start.getMinute(), end.getHour(), end.getMinute());
    
    Label lTime = new Label("🕒 " + strTime);
    lTime.getStyleClass().add("seance-time");
    
    // Salle
    Label sNam = new Label("🏢 " + salleMap.getOrDefault(s.getSalleId(), "S.Inc"));
    sNam.getStyleClass().add("seance-time");
    
    bloc.getChildren().addAll(mName, lTime, sNam);
    return bloc;
}
```

---

### 5. **Navigation Temporelle**

#### 5.1 Gestion des Semaines

```java
private LocalDate currentWeekStart = 
    LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

@FXML private Label lblSemaineRange;
@FXML private Label lblSemaineBadge;

/**
 * Passe à la semaine précédente
 */
@FXML private void handlePrevWeek() {
    currentWeekStart = currentWeekStart.minusDays(7);
    refreshViewForCurrentWeek();
}

/**
 * Passe à la semaine suivante
 */
@FXML private void handleNextWeek() {
    currentWeekStart = currentWeekStart.plusDays(7);
    refreshViewForCurrentWeek();
}

/**
 * Retourne à la semaine courante (aujourd'hui)
 */
@FXML private void handleToday() {
    currentWeekStart = 
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    refreshViewForCurrentWeek();
}
```

#### 5.2 Affichage de la Période

```java
private void setupDateRange() {
    
    LocalDate saturday = currentWeekStart.plusDays(5);  // Samedi
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    lblSemaineRange.setText(
        "Semaine du " + currentWeekStart.format(dtf) + 
        " au " + saturday.format(dtf)
    );
}
```

#### 5.3 Badge Contextuel

```java
private void updateWeekBadge() {
    
    LocalDate todayMon = 
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    
    if (currentWeekStart.isEqual(todayMon)) {
        lblSemaineBadge.setText("Cette semaine");
        lblSemaineBadge.setStyle(
            "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; " +
            "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;"
        );
    } else if (currentWeekStart.isEqual(todayMon.plusDays(7))) {
        lblSemaineBadge.setText("Semaine prochaine");
        lblSemaineBadge.setStyle(
            "-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; " +
            "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;"
        );
    } else if (currentWeekStart.isEqual(todayMon.minusDays(7))) {
        lblSemaineBadge.setText("Semaine passée");
        lblSemaineBadge.setStyle(
            "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; " +
            "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;"
        );
    } else {
        lblSemaineBadge.setText("Autre semaine");
        lblSemaineBadge.setStyle(
            "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; " +
            "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;"
        );
    }
}
```

---

### 6. **Export en PDF**

#### 6.1 Capture d'Écran JavaFX

```java
/**
 * Exporte le calendrier en PDF via capture d'écran
 */
@FXML
private void exportToPdf() {
    
    try {
        // 1. Prendre un snapshot du calendrier
        SnapshotParameters snapshotParams = new SnapshotParameters();
        WritableImage snapshot = calendarContainer.snapshot(snapshotParams, null);
        
        // 2. Convertir en BufferedImage
        BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
        
        // 3. Convertir en tableau de bytes (PNG)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        // 4. Créer l'image iText
        Image pdfImg = Image.getInstance(imageInByte);

        // 5. Créer le document PDF (Mode Paysage)
        Document doc = new Document(
            new com.itextpdf.text.Rectangle(
                pdfImg.getWidth() + 40, 
                pdfImg.getHeight() + 40
            )
        );
        
        // 6. Définir le chemin de sauvegarde
        String userHome = System.getProperty("user.home");
        String savePath = userHome + File.separator + "Downloads" + 
                        File.separator + "Emploi_Du_Temps.pdf";
        
        // 7. Écrire le PDF
        PdfWriter.getInstance(doc, new FileOutputStream(savePath));
        doc.open();
        pdfImg.setAbsolutePosition(20, 20);  // 20px de marge
        doc.add(pdfImg);
        doc.close();

        // 8. Afficher le succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("PDF généré avec succès !");
        alert.setContentText("Le fichier a été enregistré dans : \n" + savePath);
        alert.showAndWait();

    } catch (Exception e) {
        e.printStackTrace();
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText("La génération PDF a échoué.");
        a.setContentText(e.getMessage());
        a.showAndWait();
    }
}
```

---

## 🎨 Styling CSS

### 1. **Cartes de Statistiques**

```css
.stat-card {
    -fx-background-color: white;
    -fx-background-radius: 10;
    -fx-padding: 20;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);
}

.stat-card-border-top {
    -fx-border-color: #ef4444 transparent transparent transparent;
    -fx-border-width: 4 0 0 0;
    -fx-border-radius: 10;
}

.stat-value {
    -fx-font-size: 36px;
    -fx-font-weight: 900;
    -fx-text-fill: #1e293b;
}

.stat-label {
    -fx-font-size: 13px;
    -fx-text-fill: #64748b;
}
```

### 2. **Boxes de Charge par Jour**

```css
.charge-day-box {
    -fx-background-radius: 8;
    -fx-padding: 15 10;
    -fx-alignment: center;
    -fx-min-width: 65px;
}

.charge-day-box.empty {
    -fx-background-color: #94a3b8;
}

.charge-day-box.active {
    -fx-background-color: #10b981;
}

.charge-day-label {
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-font-size: 13px;
}

.charge-day-val {
    -fx-text-fill: white;
    -fx-font-size: 18px;
    -fx-font-weight: 900;
}
```

### 3. **Calendrier Grid**

```css
.grid-container {
    -fx-background-color: white;
    -fx-background-radius: 10;
    -fx-padding: 20;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);
}

.grid-cell {
    -fx-border-color: #f1f5f9;
    -fx-border-width: 1;
}

.seance-block {
    -fx-background-radius: 6;
    -fx-padding: 5 8;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);
}

.seance-title {
    -fx-font-weight: bold;
    -fx-font-size: 12px;
    -fx-text-fill: white;
}

.seance-time {
    -fx-font-size: 10px;
    -fx-text-fill: rgba(255, 255, 255, 0.9);
}
```

---

## 📈 Métriques Calculées

### 1. **Charge de Travail Hebdomadaire**

```java
/**
 * Calcule la charge de travail totale de la semaine
 */
public double calculateWeeklyWorkload() {
    
    double totalHours = seances.stream()
        .filter(s -> s.getHeureDebut() != null && s.getHeureFin() != null)
        .mapToDouble(s -> {
            long diffMs = s.getHeureFin().getTime() - s.getHeureDebut().getTime();
            return diffMs / (1000.0 * 60 * 60);
        })
        .sum();
    
    return totalHours;
}
```

### 2. **Maximum de Séances par Jour**

```java
/**
 * Trouve le jour le plus chargé
 */
public int getMaxSeancesPerDay() {
    
    String[] jFull = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    
    int max = 0;
    
    for (String day : jFull) {
        int count = (int) seances.stream()
            .filter(s -> s.getJour().equalsIgnoreCase(day))
            .count();
        
        if (count > max) max = count;
    }
    
    return max;
}
```

### 3. **Distribution des Matières**

```java
/**
 * Compte le nombre de séances par matière
 */
public Map<String, Integer> getMatiereDistribution() {
    
    Map<String, Integer> distribution = new HashMap<>();
    
    for (Seance s : seances) {
        String matiereName = matiereMap.getOrDefault(s.getMatiereId(), "Inconnu");
        distribution.put(matiereName, distribution.getOrDefault(matiereName, 0) + 1);
    }
    
    return distribution;
}
```

---

## 🚀 Performance et Optimisation

### 1. **Lazy Loading des Données**

```java
private List<Seance> cachedSeances = null;

public List<Seance> getCachedSeances() {
    
    if (cachedSeances == null) {
        try {
            cachedSeances = seanceService.getAllSeances();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    return cachedSeances;
}
```

### 2. **Pagination pour Grandes Données**

```java
private int currentWeekIndex = 0;

/**
 * Charge uniquement les données de la semaine courante
 */
private void loadWeekData(int weekOffset) {
    
    LocalDate weekStart = 
        LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .plusDays(weekOffset * 7);
    
    LocalDate weekEnd = weekStart.plusDays(6);
    
    seances = allClassSeances.stream().filter(s -> {
        if (s.getHeureDebut() == null) return false;
        LocalDate sd = s.getHeureDebut().toLocalDateTime().toLocalDate();
        return !sd.isBefore(weekStart) && !sd.isAfter(weekEnd);
    }).toList();
}
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **Framework UI** | JavaFX |
| **Graphiques** | BarChart (ChartFX) |
| **Navigation** | Navigation temporelle (semaines) |
| **KPIs** | Séances, Matières, Salles, Heures |
| **Charge par jour** | Box visuelle avec statistiques |
| **Calendrier** | GridPane avec placement dynamique |
| **Export** | PDF via iText + Snapshot |
| **Styling** | CSS personnalisé |
| **Performance** | Stream API + Lazy Loading |

---

## 🔗 Ressources

- **JavaFX Documentation** : https://openjfx.io/
- **iText Library** : https://itextpdf.com/
- **Java Time API** : Oracle Documentation

---

**Documentation générée pour EspritFlow - Statistiques Front Office** 📊
