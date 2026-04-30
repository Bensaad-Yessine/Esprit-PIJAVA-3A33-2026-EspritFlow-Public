# 📄 Documentation Fonctionnalité : Export PDF de l'Emploi du Temps

## 📋 Vue d'ensemble

La fonctionnalité d'export PDF permet aux étudiants et enseignants de télécharger leur emploi du temps au format PDF pour une impression ou un archivage hors ligne. Le PDF est généré à partir d'une capture d'écran haute qualité de l'interface JavaFX.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **Bibliothèques Java**
- **iText 5.5+** : Génération de documents PDF
  - `com.itextpdf.text.Document` : Conteneur du PDF
  - `com.itextpdf.text.Image` : Insertion d'images
  - `com.itextpdf.text.pdf.PdfWriter` : Écriture du fichier
  - `com.itextpdf.text.Rectangle` : Configuration des dimensions

### 2. **JavaFX Graphics**
- **SnapshotParameters** : Configuration de la capture
- **WritableImage** : Image en mémoire
- **SwingFXUtils** : Conversion JavaFX ↔ AWT
- **BufferedImage** : Image standard Java AWT

### 3. **Java I/O**
- **ByteArrayOutputStream** : Flux de sortie en mémoire
- **FileOutputStream** : Écriture de fichiers
- **ImageIO (javax.imageio)** : Manipulation d'images

---

## 🔧 Architecture du Système

### 1. **Flux de Génération PDF**

```
Interface JavaFX (CalendarContainer)
    ↓
SnapshotParameters (configuration)
    ↓
calendarContainer.snapshot() (capture)
    ↓
WritableImage (image JavaFX)
    ↓
SwingFXUtils.fromFXImage() (conversion)
    ↓
BufferedImage (image AWT)
    ↓
ImageIO.write() (formatage)
    ↓
ByteArrayOutputStream (bytes)
    ↓
Image.getInstance(iText) (image PDF)
    ↓
Document + PdfWriter (génération)
    ↓
Fichier PDF sur disque
```

---

## 💻 Implémentation Technique

### 1. **Méthode Principale d'Export**

```java
/**
 * Exporte le calendrier en PDF
 * 
 * Processus :
 * 1. Capture d'écran du calendrier JavaFX
 * 2. Conversion en image PNG
 * 3. Génération PDF avec iText
 * 4. Sauvegarde dans le dossier Downloads
 */
@FXML
private void exportToPdf() {
    
    try {
        // Étape 1 : Capture d'écran
        SnapshotParameters snapshotParams = new SnapshotParameters();
        WritableImage snapshot = calendarContainer.snapshot(snapshotParams, null);
        
        // Étape 2 : Conversion en BufferedImage
        BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
        
        // Étape 3 : Conversion en tableau de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        // Étape 4 : Création de l'image iText
        Image pdfImg = Image.getInstance(imageInByte);

        // Étape 5 : Configuration du document
        // Mode Paysage pour accommoder la largeur du calendrier
        Document doc = new Document(
            new com.itextpdf.text.Rectangle(
                pdfImg.getWidth() + 40,  // Largeur : image + marge
                pdfImg.getHeight() + 40   // Hauteur : image + marge
            )
        );
        
        // Étape 6 : Définition du chemin de sauvegarde
        String userHome = System.getProperty("user.home");
        String savePath = userHome + File.separator + "Downloads" + 
                        File.separator + "Emploi_Du_Temps.pdf";
        
        // Étape 7 : Génération du PDF
        PdfWriter.getInstance(doc, new FileOutputStream(savePath));
        doc.open();
        
        // Étape 8 : Insertion de l'image avec marges
        pdfImg.setAbsolutePosition(20, 20);
        doc.add(pdfImg);
        
        // Étape 9 : Fermeture du document
        doc.close();

        // Étape 10 : Notification de succès
        showSuccessAlert(savePath);

    } catch (Exception e) {
        e.printStackTrace();
        showErrorAlert(e.getMessage());
    }
}
```

---

### 2. **Configuration du Snapshot**

```java
/**
 * Capture le calendrier avec des paramètres avancés
 */
private WritableImage captureCalendar() {
    
    SnapshotParameters snapshotParams = new SnapshotParameters();
    
    // Activer l'anti-aliasing pour une meilleure qualité
    snapshotParams.setFill(Color.TRANSPARENT);  // Fond transparent
    
    // Définir la zone de capture (optionnel)
    // Par défaut : tout le container
    // snapshotParams.setViewport(new Rectangle2D(x, y, width, height));
    
    // Capturer
    WritableImage snapshot = calendarContainer.snapshot(snapshotParams, null);
    
    return snapshot;
}
```

**Paramètres de Snapshot :**

| Paramètre | Valeur | Description |
|-----------|--------|-------------|
| `setFill()` | `TRANSPARENT` | Fond transparent (pas de blanc) |
| `setViewport()` | Optionnel | Zone spécifique à capturer |
| `setDepthBuffer()` | `false` | Désactiver pour performances |

---

### 3. **Conversion Image JavaFX → AWT**

```java
import javafx.embed.swing.SwingFXUtils;

/**
 * Convertit une image JavaFX en BufferedImage (AWT)
 * 
 * Pourquoi ?
 * - ImageIO ne travaille qu'avec BufferedImage
 * - iText nécessite BufferedImage ou bytes
 */
private BufferedImage convertToAWT(WritableImage fxImage) {
    
    BufferedImage awtImage = SwingFXUtils.fromFXImage(fxImage, null);
    
    // Optionnel : Augmenter la qualité
    // BufferedImage highQuality = new BufferedImage(
    //     awtImage.getWidth() * 2,
    //     awtImage.getHeight() * 2,
    //     BufferedImage.TYPE_INT_RGB
    // );
    // Graphics2D g = highQuality.createGraphics();
    // g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
    //                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    // g.drawImage(awtImage, 0, 0, highQuality.getWidth(), 
    //             highQuality.getHeight(), null);
    // g.dispose();
    
    return awtImage;
}
```

---

### 4. **Encodage de l'Image**

```java
/**
 * Encode l'image en tableau de bytes (format PNG)
 */
private byte[] encodeImageToBytes(BufferedImage image) throws IOException {
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    // Format PNG pour qualité maximale
    String format = "png";
    
    // Écriture de l'image
    ImageIO.write(image, format, baos);
    
    // Récupération des bytes
    byte[] imageBytes = baos.toByteArray();
    
    // Nettoyage
    baos.flush();
    baos.close();
    
    return imageBytes;
}
```

**Formats d'image supportés :**

| Format | Avantages | Inconvénients |
|--------|-----------|---------------|
| **PNG** | Qualité parfaite, transparence | Taille plus grande ⭐ |
| **JPEG** | Taille réduite | Compression avec perte |
| **GIF** | Animation | 256 couleurs max |

---

### 5. **Création du Document iText**

```java
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Crée un document PDF avec les dimensions optimales
 */
private Document createPDFDocument(Image pdfImg) {
    
    // Calculer les dimensions avec marges
    float margin = 20;  // 20 points (~7mm)
    
    float docWidth = pdfImg.getWidth() + (2 * margin);
    float docHeight = pdfImg.getHeight() + (2 * margin);
    
    // Créer le rectangle de page
    Rectangle pageSize = new Rectangle(docWidth, docHeight);
    
    // Créer le document
    Document doc = new Document(pageSize, margin, margin, margin, margin);
    
    return doc;
}
```

**Unités iText :**
- 1 point (pt) = 1/72 pouce ≈ 0.353 mm
- 1 mm = 2.83 points

---

### 6. **Écriture du PDF**

```java
/**
 * Génère le fichier PDF sur le disque
 */
private void writePDFDocument(Document doc, Image pdfImg, String filePath) 
    throws Exception {
    
    // Créer le writer
    PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
    
    // Ouvrir le document
    doc.open();
    
    // Insérer l'image
    pdfImg.setAbsolutePosition(20, 20);  // Position (x, y) en points
    doc.add(pdfImg);
    
    // Fermer le document (sauvegarde automatique)
    doc.close();
    
    // Le writer est fermé automatiquement par doc.close()
}
```

---

### 7. **Gestion des Chemins de Fichier**

```java
import java.io.File;

/**
 * Génère le chemin de sauvegarde du PDF
 */
private String getSavePath() {
    
    // Récupérer le répertoire Home de l'utilisateur
    String userHome = System.getProperty("user.home");
    
    // Construire le chemin
    String savePath = userHome + 
                      File.separator + 
                      "Downloads" + 
                      File.separator + 
                      "Emploi_Du_Temps.pdf";
    
    // Windows : C:\Users\Utilisateur\Downloads\Emploi_Du_Temps.pdf
    // Linux/Mac : /home/utilisateur/Downloads/Emploi_Du_Temps.pdf
    
    return savePath;
}

/**
 * Vérifie si le dossier existe, le crée sinon
 */
private void ensureDirectoryExists(String filePath) {
    
    File file = new File(filePath);
    File parentDir = file.getParentFile();
    
    if (!parentDir.exists()) {
        parentDir.mkdirs();  // Créer tous les dossiers parents
    }
}
```

---

### 8. **Alertes Utilisateur**

```java
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Affiche une alerte de succès
 */
private void showSuccessAlert(String filePath) {
    
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Succès");
    alert.setHeaderText("PDF généré avec succès !");
    alert.setContentText("Le fichier a été enregistré dans :\n" + filePath);
    
    // Personnalisation du bouton
    alert.getButtonTypes().setAll(ButtonType.OK);
    
    // Affichage
    alert.showAndWait();
    
    // Optionnel : Ouvrir le dossier
    openFileLocation(filePath);
}

/**
 * Affiche une alerte d'erreur
 */
private void showErrorAlert(String errorMessage) {
    
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Erreur");
    alert.setHeaderText("La génération PDF a échoué.");
    alert.setContentText(errorMessage);
    
    alert.showAndWait();
}

/**
 * Ouvre le dossier contenant le fichier
 */
private void openFileLocation(String filePath) {
    
    try {
        File file = new File(filePath);
        
        if (file.exists()) {
            // Windows
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("explorer /select," + filePath);
            }
            // Mac
            else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Runtime.getRuntime().exec("open -R " + file.getParent());
            }
            // Linux
            else {
                Runtime.getRuntime().exec("xdg-open " + file.getParent());
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

---

## 🎨 Personnalisation Avancée

### 1. **Ajout d'un En-Tête PDF**

```java
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;

/**
 * Ajoute un en-tête au PDF
 */
private void addPDFHeader(Document doc) throws Exception {
    
    // Créer un tableau pour l'en-tête
    PdfPTable headerTable = new PdfPTable(3);
    headerTable.setWidthPercentage(100);
    
    // Colonne 1 : Logo
    PdfPCell logoCell = new PdfPCell();
    logoCell.addElement(new Phrase("🎓 EspritFlow"));
    logoCell.setBorder(Rectangle.NO_BORDER);
    headerTable.addCell(logoCell);
    
    // Colonne 2 : Titre
    PdfPCell titleCell = new PdfPCell();
    titleCell.addElement(new Phrase("Emploi du Temps"));
    titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    titleCell.setBorder(Rectangle.NO_BORDER);
    headerTable.addCell(titleCell);
    
    // Colonne 3 : Date
    PdfPCell dateCell = new PdfPCell();
    dateCell.addElement(new Phrase(
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    ));
    dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    dateCell.setBorder(Rectangle.NO_BORDER);
    headerTable.addCell(dateCell);
    
    // Ajouter au document
    doc.add(headerTable);
    
    // Ligne de séparation
    doc.add(new Paragraph(new LineSeparator(1.0f, 100.0f, 
        BaseColor.BLACK, Element.ALIGN_LEFT, -5)));
}
```

### 2. **Ajout d'un Pied de Page**

```java
import com.itextpdf.text.pdf.PdfPageEventHelper;

/**
 * Classe personnalisée pour le pied de page
 */
class FooterEvent extends PdfPageEventHelper {
    
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        
        // Créer un pied de page
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        
        PdfPCell cell = new PdfPCell(new Phrase(
            "Généré par EspritFlow - " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        footer.addCell(cell);
        
        // Positionner en bas de page
        float bottom = document.bottomMargin() + 20;
        footer.writeSelectedRows(0, -1, 0, bottom, writer.getDirectContent());
    }
}

/**
 * Appliquer le pied de page
 */
private void applyFooter(Document doc, PdfWriter writer) {
    
    FooterEvent footerEvent = new FooterEvent();
    writer.setPageEvent(footerEvent);
}
```

### 3. **Compression du PDF**

```java
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * Compresse le PDF généré
 */
private void compressPDF(String inputPath, String outputPath) throws Exception {
    
    PdfReader reader = new PdfReader(inputPath);
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
    
    // Compression des flux
    stamper.setFullCompression(true);
    stamper.setCompressionLevel(PdfStream.BEST_COMPRESSION);
    
    stamper.close();
    reader.close();
}
```

---

## 📊 Gestion des Erreurs

### 1. **Capture d'Erreurs**

```java
/**
 * Export avec gestion complète des erreurs
 */
@FXML
private void exportToPdf() {
    
    try {
        // Vérification préalable
        if (calendarContainer == null) {
            throw new IllegalStateException("CalendarContainer non initialisé");
        }
        
        // Vérification des données
        if (seances.isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Attention");
            confirm.setHeaderText("Aucune séance à exporter");
            confirm.setContentText("L'emploi du temps est vide. Voulez-vous continuer ?");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response != ButtonType.OK) return;
            });
        }
        
        // Génération
        performExport();
        
    } catch (OutOfMemoryError e) {
        showErrorAlert("Mémoire insuffisante. Essayez de redémarrer l'application.");
    } catch (IOException e) {
        showErrorAlert("Erreur d'écriture : " + e.getMessage());
    } catch (DocumentException e) {
        showErrorAlert("Erreur de création PDF : " + e.getMessage());
    } catch (Exception e) {
        e.printStackTrace();
        showErrorAlert("Erreur inconnue : " + e.getMessage());
    }
}
```

### 2. **Validation des Chemins**

```java
/**
 * Valide que le chemin de sauvegarde est accessible
 */
private boolean validateSavePath(String path) {
    
    File file = new File(path);
    File parentDir = file.getParentFile();
    
    // Vérifier que le dossier existe
    if (!parentDir.exists()) {
        return false;
    }
    
    // Vérifier que le dossier est accessible en écriture
    if (!parentDir.canWrite()) {
        return false;
    }
    
    // Vérifier que le fichier n'est pas déjà ouvert
    if (file.exists() && !file.canWrite()) {
        return false;
    }
    
    return true;
}
```

---

## 🚀 Optimisations de Performance

### 1. **Cache d'Image**

```java
private byte[] cachedCalendarImage = null;

/**
 * Réutilise l'image si elle n'a pas changé
 */
public byte[] getCachedOrNewImage() throws IOException {
    
    // Si l'image est en cache et le calendrier n'a pas changé
    if (cachedCalendarImage != null && !calendarModified) {
        return cachedCalendarImage;
    }
    
    // Générer une nouvelle image
    WritableImage snapshot = calendarContainer.snapshot(null, null);
    BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bImage, "png", baos);
    
    cachedCalendarImage = baos.toByteArray();
    calendarModified = false;
    
    return cachedCalendarImage;
}
```

### 2. **Génération Asynchrone**

```java
import javafx.concurrent.Task;

/**
 * Génère le PDF en arrière-plan
 */
@FXML
private void exportToPdfAsync() {
    
    // Créer une tâche de fond
    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            
            // Mise à jour du progrès
            updateMessage("Capture d'écran en cours...");
            updateProgress(0.1, 1.0);
            
            // Capturer
            WritableImage snapshot = calendarContainer.snapshot(null, null);
            
            updateMessage("Conversion en cours...");
            updateProgress(0.5, 1.0);
            
            // Convertir
            BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
            
            updateMessage("Génération PDF en cours...");
            updateProgress(0.8, 1.0);
            
            // Générer PDF
            generatePDF(bImage);
            
            updateMessage("Terminé !");
            updateProgress(1.0, 1.0);
            
            return null;
        }
    };
    
    // Gérer le succès
    task.setOnSucceeded(e -> {
        showSuccessAlert(getSavePath());
    });
    
    // Gérer l'échec
    task.setOnFailed(e -> {
        showErrorAlert(task.getException().getMessage());
    });
    
    // Lancer la tâche
    new Thread(task).start();
    
    // Afficher une barre de progression
    ProgressBar progressBar = new ProgressBar();
    progressBar.progressProperty().bind(task.progressProperty());
    
    Label statusLabel = new Label();
    statusLabel.textProperty().bind(task.messageProperty());
}
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **Bibliothèque PDF** | iText 5.5+ |
| **Capture UI** | JavaFX Snapshot API |
| **Conversion** | SwingFXUtils (JavaFX → AWT) |
| **Format Image** | PNG (qualité parfaite) |
| **Dimensions** | Automatiques (image + marges) |
| **Sauvegarde** | Dossier Downloads utilisateur |
| **Personnalisation** | Header/Footer possible |
| **Performance** | Cache + Async optionnel |

---

## 🔗 Ressources

- **iText Documentation** : https://itextpdf.com/
- **JavaFX Snapshot API** : Oracle Documentation
- **PDF Specification** : ISO 32000

---

**Documentation générée pour EspritFlow - Export PDF de l'Emploi du Temps** 📄
