# 📂 Fichiers Export PDF Emploi du Temps - Localisation Complète

## 🎯 Vue d'ensemble
Cette fonctionnalité permet d'exporter l'emploi du temps au format PDF avec une mise en page professionnelle, incluant toutes les séances de l'utilisateur.

---

## 📁 Structure des Fichiers

### 1️⃣ Contrôleurs (Controllers)

#### EmploiContentController.java
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`

**Responsabilité** : Affichage de l'emploi du temps et export PDF

**Méthodes clés** :

```java
// Ligne 40-70 : Déclarations des composants
@FXML
private VBox scheduleContainer;
@FXML
private Button exportButton;
@FXML
private Button sendEmailButton;
@FXML
private Button addToAgendaButton;

// Services
private SeanceService seanceService;
private MatiereService matiereService;
private SalleService salleService;

// Ligne 75-100 : Initialisation
@Override
public void initialize(URL location, ResourceBundle resources) {
    seanceService = new SeanceService();
    matiereService = new MatiereService();
    salleService = new SalleService();
    loadEmploiDuTemps();
}

// Ligne 105-150 : Chargement de l'emploi du temps
private void loadEmploiDuTemps() {
    user currentUser = SessionManager.getInstance().getCurrentUser();
    List<Seance> seances = seanceService.getAllSeances();
    
    // Filtrer les séances de l'utilisateur
    List<Seance> userSeances = seances.stream()
        .filter(s -> s.getClasseId().equals(currentUser.getClasse_id()))
        .collect(Collectors.toList());
    
    // Afficher le calendrier hebdomadaire
    renderWeeklySchedule(userSeances);
}

// Ligne 155-220 : Export PDF de l'emploi du temps
@FXML
private void exportToPDF() {
    try {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        List<Seance> seances = seanceService.getAllSeances();
        List<Seance> userSeances = seances.stream()
            .filter(s -> s.getClasseId().equals(currentUser.getClasse_id()))
            .collect(Collectors.toList());
        
        // Nom du fichier PDF
        String fileName = "Emploi_du_temps_" + currentUser.getNom() + "_" + 
                         currentUser.getPrenom() + "_" + 
                         LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        
        // Générer le PDF
        generatePDF(userSeances, currentUser, fileName);
        
        showAlert("Succès", "L'emploi du temps a été exporté en PDF : " + fileName);
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Impossible d'exporter l'emploi du temps en PDF");
    }
}

// Ligne 225-320 : Génération du PDF avec iText
private void generatePDF(List<Seance> seances, user currentUser, String fileName) {
    try {
        // Configuration du document
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        
        // Police arabe
        BaseFont arabicFont = BaseFont.createFont(
            "C:/Windows/Fonts/arial.ttf", 
            BaseFont.IDENTITY_H, 
            BaseFont.EMBEDDED
        );
        Font titleFont = new Font(arabicFont, 18, Font.BOLD);
        Font headerFont = new Font(arabicFont, 12, Font.BOLD);
        Font normalFont = new Font(arabicFont, 10, Font.NORMAL);
        
        // Ligne 330-360 : Titre du document
        Paragraph title = new Paragraph("Emploi du Temps", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        title.setSpacingAfter(20);
        
        // Ligne 365-390 : Informations utilisateur
        Paragraph userInfo = new Paragraph();
        userInfo.add(new Chunk("Étudiant : ", headerFont));
        userInfo.add(new Chunk(currentUser.getNom() + " " + currentUser.getPrenom(), normalFont));
        userInfo.add(Chunk.NEWLINE);
        userInfo.add(new Chunk("Date : ", headerFont));
        userInfo.add(new Chunk(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
        userInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(userInfo);
        userInfo.setSpacingAfter(20);
        
        // Ligne 395-450 : Création du tableau
        PdfPTable table = new PdfPTable(8); // 8 colonnes (heure + 7 jours)
        table.setWidthPercentage(100);
        
        // En-têtes
        String[] days = {"Heure", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (String day : days) {
            PdfPCell cell = new PdfPCell(new Phrase(day, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        // Ligne 455-530 : Remplissage du tableau
        // Créneaux de 8h à 18h
        for (int hour = 8; hour < 18; hour++) {
            // Heure
            PdfPCell hourCell = new PdfPCell(new Phrase(hour + ":00 - " + (hour + 1) + ":00", headerFont));
            hourCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            hourCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hourCell);
            
            // Pour chaque jour
            String[] dayNames = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            for (String day : dayNames) {
                // Trouver la séance correspondante
                Optional<Seance> optSeance = seances.stream()
                    .filter(s -> s.getJour().equals(day) && 
                               s.getHeureDebut().toLocalDateTime().getHour() == hour)
                    .findFirst();
                
                PdfPCell cell;
                if (optSeance.isPresent()) {
                    Seance s = optSeance.get();
                    String matiereName = matiereService.getById(s.getMatiereId()).getNom();
                    String salleName = salleService.getById(s.getSalleId()).getBlock() + 
                                     salleService.getById(s.getSalleId()).getNumber();
                    
                    // Couleur selon le type de séance
                    BaseColor bgColor = BaseColor.WHITE;
                    if ("Révision".equals(s.getTypeSeance())) {
                        bgColor = new BaseColor(139, 92, 246); // Violet
                    }
                    
                    cell = new PdfPCell();
                    cell.setBackgroundColor(bgColor);
                    
                    Phrase phrase = new Phrase();
                    phrase.add(new Chunk(matiereName, headerFont));
                    phrase.add(Chunk.NEWLINE);
                    phrase.add(new Chunk(salleName, normalFont));
                    cell.addElement(phrase);
                } else {
                    cell = new PdfPCell(new Phrase("", normalFont));
                }
                
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                cell.setMinimumHeight(40);
                table.addCell(cell);
            }
        }
        
        document.add(table);
        document.close();
        
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Erreur lors de la génération du PDF", e);
    }
}
```

**Où trouver les données** :
- Séances : `src/main/java/piJava/services/SeanceService.java`
- Matières : `src/main/java/piJava/services/MatiereService.java`
- Salles : `src/main/java/piJava/services/SalleService.java`
- Utilisateur : `SessionManager.getInstance().getCurrentUser()`

---

#### SeanceContentController.java (Backoffice)
**Emplacement** : `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java`

**Responsabilité** : Export PDF de l'emploi du temps pour une classe

**Méthodes clés** :

```java
// Ligne 500-540 : Export PDF pour une classe
@FXML
private void exportClassScheduleToPDF() {
    Classe selectedClasse = getSelectedClasse();
    if (selectedClasse == null) {
        showAlert("Erreur", "Veuillez sélectionner une classe");
        return;
    }
    
    try {
        List<Seance> seances = seanceService.getAllSeances();
        List<Seance> classSeances = seances.stream()
            .filter(s -> s.getClasseId().equals(selectedClasse.getId()))
            .collect(Collectors.toList());
        
        String fileName = "Emploi_du_temps_Classe_" + selectedClasse.getNom() + "_" + 
                         LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        
        generatePDF(classSeances, selectedClasse, fileName);
        showAlert("Succès", "L'emploi du temps a été exporté en PDF");
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Impossible d'exporter l'emploi du temps");
    }
}
```

---

### 2️⃣ Services (Business Logic)

#### SeanceService.java
**Emplacement** : `src/main/java/piJava/services/SeanceService.java`

**Méthodes utilisées pour l'export PDF** :

```java
// Ligne 30-60 : Récupérer toutes les séances
public List<Seance> getAllSeances() {
    List<Seance> list = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM seance ORDER BY heure_debut";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            Seance s = new Seance();
            s.setId(rs.getInt("id"));
            s.setJour(rs.getString("jour"));
            s.setTypeSeance(rs.getString("type_seance"));
            s.setMode(rs.getString("mode"));
            s.setHeureDebut(rs.getTimestamp("heure_debut"));
            s.setHeureFin(rs.getTimestamp("heure_fin"));
            s.setSalleId(rs.getInt("salle_id"));
            s.setClasseId(rs.getInt("classe_id"));
            s.setMatiereId(rs.getInt("matiere_id"));
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
- Colonnes utilisées : `id`, `jour`, `type_seance`, `mode`, `heure_debut`, `heure_fin`, `salle_id`, `classe_id`, `matiere_id`

---

#### MatiereService.java
**Emplacement** : `src/main/java/piJava/services/MatiereService.java`

**Méthodes utilisées pour l'export PDF** :

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

#### SalleService.java
**Emplacement** : `src/main/java/piJava/services/SalleService.java`

**Méthodes utilisées pour l'export PDF** :

```java
// Ligne 80-105 : Récupérer une salle par ID
public Salle getById(int id) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM salle WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Salle s = new Salle();
            s.setId(rs.getInt("id"));
            s.setBlock(rs.getString("block"));
            s.setNumber(rs.getString("number"));
            s.setCapacity(rs.getInt("capacity"));
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
- Table BDD : `salle`
- Colonnes utilisées : `id`, `block`, `number`, `capacity`

---

### 3️⃣ Entités (Data Models)

#### Seance.java
**Emplacement** : `src/main/java/piJava/entities/Seance.java`

**Attributs utilisés pour l'export PDF** :

```java
// Ligne 15-40
private Integer id;
private String jour;           // Pour le placement dans le tableau
private String typeSeance;     // Pour la couleur (Révision = violet)
private String mode;           // Présentiel / En ligne
private Timestamp heureDebut;  // Pour le créneau horaire
private Timestamp heureFin;
private Integer salleId;       // Pour afficher le nom de la salle
private Integer classeId;      // Pour filtrer par classe
private Integer matiereId;     // Pour afficher le nom de la matière
```

---

#### Matiere.java
**Emplacement** : `src/main/java/piJava/entities/Matiere.java`

**Attributs utilisés pour l'export PDF** :

```java
// Ligne 15-30
private Integer id;
private String nom;            // Affiché dans le PDF
private String code;
```

---

#### Salle.java
**Emplacement** : `src/main/java/piJava/entities/Salle.java`

**Attributs utilisés pour l'export PDF** :

```java
// Ligne 10-25
private Integer id;
private String block;          // Affiché dans le PDF (ex: "A")
private String number;         // Affiché dans le PDF (ex: "101")
private Integer capacity;
```

---

### 4️⃣ Fichiers FXML (UI Views)

#### EmploiContent.fxml
**Emplacement** : `src/main/resources/frontoffice/emploi/EmploiContent.fxml`

**Bouton d'export PDF** :

```xml
<!-- Ligne 20-25 : Bouton Export PDF -->
<Button fx:id="exportButton" text="Exporter PDF" onAction="#exportToPDF" styleClass="export-button"/>

<!-- Ligne 30-35 : Conteneur du calendrier -->
<VBox fx:id="scheduleContainer" styleClass="schedule-container"/>
```

---

### 5️⃣ Styles CSS

#### emploi.css
**Emplacement** : `src/main/resources/frontoffice/emploi/emploi.css`

**Styles pour le bouton d'export** :

```css
/* Ligne 15-25 */
.export-button {
    -fx-background-color: #dc2626;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-cursor: hand;
    -fx-padding: 10 20;
}

.export-button:hover {
    -fx-background-color: #b91c1c;
}
```

---

## 📊 Flux de Données

### Export PDF
```
[Utilisateur clique sur "Exporter PDF"]
        ↓
[EmploiContentController.exportToPDF()]
        ↓
[SeanceService.getAllSeances()]
        ↓
[Filtrage par classe utilisateur]
        ↓
[generatePDF()]
        ↓
[MatiereService.getById()] → Nom matière
        ↓
[SalleService.getById()] → Nom salle
        ↓
[iText: Création document PDF]
        ↓
[Fichier PDF enregistré]
```

---

## 🔍 Comment Trouver les Données

### Pour modifier le format du PDF :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`
2. **Méthode** : `generatePDF()` (lignes 225-530)
3. **Éléments modifiables** :
   - Taille de la page : `PageSize.A4`
   - Police : Chemin vers le fichier TTF
   - Taille de police : `new Font(arabicFont, 18, Font.BOLD)`
   - Nombre de colonnes : `new PdfPTable(8)`
   - Couleurs : `BaseColor.LIGHT_GRAY`, `BaseColor.WHITE`

### Pour modifier les créneaux horaires dans le PDF :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`
2. **Méthode** : `generatePDF()` (lignes 455-530)
3. **Boucle** : Modifier `for (int hour = 8; hour < 18; hour++)`

### Pour modifier le nom du fichier PDF généré :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`
2. **Méthode** : `exportToPDF()` (lignes 155-220)
3. **Variable** : `fileName` (lignes 160-163)

### Pour ajouter des informations supplémentaires au PDF :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java`
2. **Méthode** : `generatePDF()`
3. **Ajouter** : Créer des `Paragraph` ou `PdfPTable` supplémentaires

---

## 📄 Structure du PDF Généré

### En-tête
```
Emploi du Temps

Étudiant : NOM Prénom
Date : 30/04/2026
```

### Tableau
| Heure | Lundi | Mardi | Mercredi | Jeudi | Vendredi | Samedi | Dimanche |
|-------|-------|-------|----------|-------|----------|--------|----------|
| 8:00-9:00 | [Matiere]<br>[Salle] | ... | ... | ... | ... | ... | ... |
| 9:00-10:00 | ... | ... | ... | ... | ... | ... | ... |
| ... | ... | ... | ... | ... | ... | ... | ... |

### Couleurs
- **Normal** : Fond blanc
- **Révision** : Fond violet (#8b5cf6)
- **En-têtes** : Fond gris clair

---

## 📝 Format des Données

### Nom du fichier PDF
```
Emploi_du_temps_{Nom}_{Prenom}_{Date}.pdf
Exemple: Emploi_du_temps_BenAli_Ahmed_20260430.pdf
```

### Police arabe
```
C:/Windows/Fonts/arial.ttf
```

---

## 🚀 Points d'Entrée

### Frontoffice
- **Menu Emploi** → Bouton "Exporter PDF" → Génération et téléchargement du PDF

### Backoffice
- **Menu Séances** → Sélectionner une classe → Bouton "Exporter PDF" → Génération pour la classe

---

**Dernière mise à jour** : 30 avril 2026
