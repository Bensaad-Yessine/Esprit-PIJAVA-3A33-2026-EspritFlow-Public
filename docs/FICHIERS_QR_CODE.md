# 📂 Fichiers QR Code - Localisation Complète

## 🎯 Vue d'ensemble
Cette fonctionnalité permet de générer et scanner des QR codes pour les séances et les utilisateurs, facilitant ainsi le suivi des présences.

---

## 📁 Structure des Fichiers

### 1️⃣ Contrôleurs (Controllers)

#### A. QRCodeDisplayController.java (Backoffice - Séances)
**Emplacement** : `src/main/java/piJava/Controllers/backoffice/Seance/QRCodeDisplayController.java`

**Responsabilité** : Affichage du QR code d'une séance

**Méthodes clés** :
```java
// Ligne 15-20 : Déclarations
@FXML
private ImageView qrCodeImageView;
private String seanceId;

// Ligne 30-45 : Initialisation
public void initialize() {
    // Charger le QR code
}

// Ligne 47-65 : Affichage du QR code
public void displayQRCode(String seanceId) {
    // Génération du QR code via ZXing
    // Affichage dans l'ImageView
}

// Ligne 67-80 : Fermeture
@FXML
private void handleClose() {
    Stage stage = (Stage) qrCodeImageView.getScene().getWindow();
    stage.close();
}
```

**Où trouver les données** :
- Le QR code est généré à partir de l'ID de séance
- Les données de séance viennent de `SeanceService`
- L'image générée est stockée en mémoire temporairement

---

#### B. UserContentController.java (Backoffice - Utilisateurs)
**Emplacement** : `src/main/java/piJava/Controllers/backoffice/User/UserContentController.java`

**Responsabilité** : Génération de QR code pour un utilisateur

**Méthodes clés** :
```java
// Ligne 200-220 : Génération de QR code utilisateur
private void generateQRCodeForUser(user selectedUser) {
    try {
        // Utilisation de ZXing
        String qrData = "USER:" + selectedUser.getId() + ":" + selectedUser.getEmail();
        
        // Génération de l'image
        BufferedImage qrImage = generateQRCodeImage(qrData, 300, 300);
        
        // Affichage dans une nouvelle fenêtre
        showQRCodeWindow(qrImage, selectedUser);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Ligne 225-245 : Affichage de la fenêtre QR code
private void showQRCodeWindow(BufferedImage image, user user) {
    // Création d'un nouveau Stage
    // Configuration de l'ImageView
    // Affichage
}
```

**Où trouver les données** :
- Données utilisateur : `src/main/java/piJava/entities/user.java`
- Les données encodées : ID utilisateur + Email
- Le QR code contient un préfixe "USER:" pour identification

---

#### C. SeanceContentController.java (Backoffice - Séances)
**Emplacement** : `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java`

**Responsabilité** : Génération de QR code pour les séances

**Méthodes clés** :
```java
// Ligne 450-470 : Génération de QR code séance
@FXML
private void generateQRCodeForSeance() {
    Seance selectedSeance = getSelectedSeance();
    if (selectedSeance != null) {
        try {
            String qrData = "SEANCE:" + selectedSeance.getId();
            BufferedImage qrImage = generateQRCodeImage(qrData, 300, 300);
            showQRCodeWindow(qrImage, selectedSeance);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de générer le QR code");
        }
    }
}

// Ligne 475-495 : Méthode utilitaire de génération
private BufferedImage generateQRCodeImage(String data, int width, int height) 
        throws WriterException {
    // Configuration du QRWriter
    // Génération de la matrice
    // Conversion en BufferedImage
}
```

**Où trouver les données** :
- Données séance : `src/main/java/piJava/entities/Seance.java`
- Les données encodées : ID séance
- Le QR code contient un préfixe "SEANCE:" pour identification

---

#### D. ScanQRCodeController.java (Frontoffice - Scan)
**Emplacement** : `src/main/java/piJava/Controllers/frontoffice/emploi/ScanQRCodeController.java`

**Responsabilité** : Scan de QR code et marquage de présence

**Méthodes clés** :
```java
// Ligne 30-50 : Déclarations
@FXML
private ImageView cameraView;
@FXML
private Label scanStatusLabel;
private ZXingReader zxingReader;

// Ligne 60-80 : Initialisation de la caméra
public void initialize() {
    zxingReader = new ZXingReader();
    startCamera();
}

// Ligne 85-120 : Démarrage de la caméra
private void startCamera() {
    // Initialisation de la caméra via JavaCV ou JavaFX Media
    // Configuration du flux vidéo
    // Détection de QR code en temps réel
}

// Ligne 125-150 : Détection de QR code
private void scanQRCode() {
    // Analyse de l'image caméra
    // Détection via ZXing
    // Validation du format (USER: ou SEANCE:)
}

// Ligne 155-180 : Marquage de présence
private void markAttendance(String qrData) {
    try {
        // Parsing des données
        if (qrData.startsWith("SEANCE:")) {
            String seanceId = qrData.split(":")[1];
            // Création d'une présence
            Attendance attendance = new Attendance();
            attendance.setSeanceId(Integer.parseInt(seanceId));
            attendance.setUserId(SessionManager.getCurrentUser().getId());
            attendance.setTimestamp(new Timestamp(System.currentTimeMillis()));
            attendanceService.add(attendance);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

**Où trouver les données** :
- Données de présence : `src/main/java/piJava/entities/Attendance.java`
- Service présence : `src/main/java/piJava/services/AttendanceService.java`
- Session utilisateur : Gérée par `SessionManager`

---

### 2️⃣ Services (Business Logic)

#### AttendanceService.java
**Emplacement** : `src/main/java/piJava/services/AttendanceService.java`

**Responsabilité** : Gestion des présences (scan QR code)

**Méthodes clés** :
```java
// Ligne 15-35 : Ajout d'une présence
public boolean add(Attendance attendance) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO attendance (seance_id, user_id, timestamp, status) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, attendance.getSeanceId());
        stmt.setInt(2, attendance.getUserId());
        stmt.setTimestamp(3, attendance.getTimestamp());
        stmt.setString(4, "PRESENT");
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 40-60 : Vérification si l'utilisateur est déjà marqué présent
public boolean isAlreadyPresent(int seanceId, int userId) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM attendance WHERE seance_id = ? AND user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, seanceId);
        stmt.setInt(2, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// Ligne 65-85 : Récupération des présences d'une séance
public List<Attendance> getBySeanceId(int seanceId) {
    List<Attendance> list = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM attendance WHERE seance_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, seanceId);
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
```

**Où trouver les données** :
- Table BDD : `attendance`
- Colonnes : `id`, `seance_id`, `user_id`, `timestamp`, `status`

---

### 3️⃣ Entités (Data Models)

#### Attendance.java
**Emplacement** : `src/main/java/piJava/entities/Attendance.java`

**Structure** :
```java
// Ligne 10-30 : Attributs
private Integer id;
private Integer seanceId;
private Integer userId;
private Timestamp timestamp;
private String status; // PRESENT, ABSENT, LATE

// Ligne 35-50 : Getters et Setters
public Integer getId() { return id; }
public void setId(Integer id) { this.id = id; }
public Integer getSeanceId() { return seanceId; }
public void setSeanceId(Integer seanceId) { this.seanceId = seanceId; }
// ... autres getters/setters
```

**Où trouver les données** :
- Ces attributs correspondent aux colonnes de la table `attendance` en BDD
- Les données sont persistées via JDBC

---

#### user.java
**Emplacement** : `src/main/java/piJava/entities/user.java`

**Attributs utilisés pour QR code** :
```java
// Ligne 15-40
private Integer id;        // ID utilisé pour générer le QR code
private String email;      // Email utilisé pour le QR code
private String nom;
private String prenom;
// ...
```

---

#### Seance.java
**Emplacement** : `src/main/java/piJava/entities/Seance.java`

**Attributs utilisés pour QR code** :
```java
// Ligne 15-35
private Integer id;        // ID utilisé pour générer le QR code
private String typeSeance;
private String jour;
private Timestamp heureDebut;
private Timestamp heureFin;
// ...
```

---

### 4️⃣ Fichiers FXML (UI Views)

#### QRCodeDisplay.fxml (Non existant - créé dynamiquement)
**Emplacement** : Génération dynamique dans le code Java

**Alternative** : La fenêtre QR code est créée programmatiquement dans les contrôleurs

**Éléments typiques** :
```xml
<ImageView fx:id="qrCodeImageView" fitWidth="300" fitHeight="300" />
<Button text="Fermer" onAction="#handleClose" />
```

---

### 5️⃣ Dépendances Maven

#### pom.xml
**Emplacement** : `pom.xml`

**Dépendances QR Code** :
```xml
<!-- ZXing Core pour génération de QR codes -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>

<!-- ZXing JavaSE pour affichage -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.1</version>
</dependency>
```

---

## 📊 Flux de Données

### Génération de QR Code
```
[Entity: user/Seance]
        ↓
[Controller: UserContentController/SeanceContentController]
        ↓
[ZXing QRWriter]
        ↓
[BufferedImage]
        ↓
[ImageView] → Affichage
```

### Scan de QR Code
```
[Camera Feed]
        ↓
[ZXingReader]
        ↓
[QR Data Parse]
        ↓
[AttendanceService]
        ↓
[Database: Table attendance]
```

---

## 🔍 Comment Trouver les Données

### Pour générer un QR code utilisateur :
1. **Fichier** : `src/main/java/piJava/Controllers/backoffice/User/UserContentController.java`
2. **Méthode** : `generateQRCodeForUser()` (lignes 200-220)
3. **Données source** : `src/main/java/piJava/entities/user.java`
4. **Outil** : ZXing (défini dans `pom.xml`)

### Pour générer un QR code séance :
1. **Fichier** : `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java`
2. **Méthode** : `generateQRCodeForSeance()` (lignes 450-470)
3. **Données source** : `src/main/java/piJava/entities/Seance.java`
4. **Outil** : ZXing (défini dans `pom.xml`)

### Pour scanner un QR code :
1. **Fichier** : `src/main/java/piJava/Controllers/frontoffice/emploi/ScanQRCodeController.java`
2. **Méthode** : `scanQRCode()` (lignes 125-150)
3. **Marquage présence** : `markAttendance()` (lignes 155-180)
4. **Données destination** : `src/main/java/piJava/entities/Attendance.java`
5. **Service** : `src/main/java/piJava/services/AttendanceService.java`

---

## 📝 Format des Données QR Code

### Format QR Code Utilisateur
```
USER:<id>:<email>
Exemple: USER:123:john.doe@esprit.tn
```

### Format QR Code Séance
```
SEANCE:<id>
Exemple: SEANCE:456
```

---

## 🚀 Points d'Entrée

### Backoffice
- **Menu Users** → Sélectionner un utilisateur → Clic droit → "Générer QR Code"
- **Menu Séances** → Sélectionner une séance → Clic droit → "Générer QR Code"

### Frontoffice
- **Menu Emploi** → Bouton "Scanner QR Code"
- **Menu Dashboard** → Bouton "Scanner QR Code"

---

**Dernière mise à jour** : 30 avril 2026
