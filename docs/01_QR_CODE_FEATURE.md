# 📄 Documentation Fonctionnalité : QR Code

## 📋 Vue d'ensemble

La fonctionnalité QR Code permet de générer des QR codes uniques pour chaque séance afin d'assurer le suivi de présence des étudiants. Chaque QR code est sécurisé avec un token unique et une date d'expiration.

---

## 🛠️ Technologies et Outils Utilisés

### 1. **Bibliothèques Java**
- **Zxing (ZXing: "Zebra Crossing")** : Bibliothèque open-source pour la génération et la lecture de QR codes
  - `com.google.zxing.qrcode.QRCodeWriter` : Génération de QR codes
  - `com.google.zxing.client.j2se.MatrixToImageWriter` : Conversion en image
  - `com.google.zxing.BarcodeFormat` : Format de code-barres (QR_CODE)
  - `com.google.zxing.common.BitMatrix` : Matrice de bits pour le QR code
  - `com.google.zxing.EncodeHintType` : Configuration de l'encodage
  - `com.google.zxing.qrcode.decoder.ErrorCorrectionLevel` : Niveau de correction d'erreur

### 2. **Formats d'image**
- **Java AWT (Abstract Window Toolkit)** : Manipulation d'images
  - `java.awt.image.BufferedImage` : Image en mémoire
  - `java.io.ByteArrayOutputStream` : Flux de sortie en mémoire
  - `javax.imageio.ImageIO` : Écriture d'images

### 3. **Base de données**
- **JDBC (Java Database Connectivity)** : Persistance des données
  - Stockage des tokens QR et dates d'expiration
  - Table `seance` avec colonnes : `qr_token`, `qr_expires_at`, `qr_url`

### 4. **API Web**
- **Servlet Java** : Génération de QR codes via URL
- **Base64** : Encodage des images pour transfert web

---

## 🔧 Architecture du Système

### 1. **Schéma de base de données**

```sql
-- Table Seance avec colonnes QR Code
ALTER TABLE seance ADD COLUMN qr_token VARCHAR(255);
ALTER TABLE seance ADD COLUMN qr_expires_at TIMESTAMP;
ALTER TABLE seance ADD COLUMN qr_url TEXT;
```

### 2. **Flux de données**

```
Séance Créée 
    ↓
Génération Token UUID 
    ↓
Calcul Date Expiration 
    ↓
Génération QR Code (ZXing)
    ↓
Conversion en PNG 
    ↓
Stockage URL (Base64 ou Chemin)
    ↓
Sauvegarde en Base de Données
```

---

## 💻 Implémentation Technique

### 1. **Génération du Token Unique**

```java
import java.util.UUID;

// Génération d'un token unique pour le QR code
String qrToken = UUID.randomUUID().toString();

// La token est utilisé pour sécuriser le QR code
// Format : "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
```

**Pourquoi UUID ?**
- **Unicité garantie** : Probabilité quasi nulle de collision
- **Standard industriel** : RFC 4122
- **Taille fixe** : 36 caractères
- **Cryptographiquement fort** : Utilise RNG de qualité

---

### 2. **Calcul de la Date d'Expiration**

```java
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

// Date de fin de séance
LocalDateTime heureFin = seance.getHeureFin().toLocalDateTime();

// Expiration 15 minutes après la fin (marge de tolérance)
LocalDateTime expiration = heureFin.plusMinutes(15);

// Conversion en Timestamp pour BDD
Timestamp qrExpiresAt = Timestamp.valueOf(expiration);
```

**Pourquoi 15 minutes de marge ?**
- Permet aux étudiants en retard de scanner le QR
- Évite les problèmes de synchronisation d'horloge
- Temps suffisant pour problèmes techniques

---

### 3. **Génération du QR Code avec ZXing**

```java
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.HashMap;

/**
 * Génère un QR code contenant le token de validation
 * 
 * @param qrToken Token unique de la séance
 * @param width Largeur du QR code en pixels
 * @param height Hauteur du QR code en pixels
 * @return Image en mémoire du QR code
 */
public BufferedImage generateQRCode(String qrToken, int width, int height) throws Exception {
    
    // Configuration de l'encodage du QR code
    HashMap<EncodeHintType, Object> hints = new HashMap<>();
    
    // Niveau de correction d'erreur : M (15%)
    // Permet de scanner même si 15% du QR est endommagé
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    
    // Encodage en UTF-8 pour supporter les caractères spéciaux
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
    
    // Marge autour du QR code (en pixels)
    hints.put(EncodeHintType.MARGIN, 1);
    
    // Création du QR code writer
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    
    // Génération de la matrice de bits du QR code
    BitMatrix bitMatrix = qrCodeWriter.encode(
        qrToken,                    // Contenu : le token unique
        BarcodeFormat.QR_CODE,       // Format : QR Code
        width,                       // Largeur
        height,                      // Hauteur
        hints                        // Configuration
    );
    
    // Conversion de la matrice en image BufferedImage
    BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    
    return qrImage;
}
```

**Paramètres expliqués :**

| Paramètre | Valeur | Description |
|-----------|--------|-------------|
| `ERROR_CORRECTION` | `M` (Medium) | Niveau de correction : 15% de redondance |
| `CHARACTER_SET` | `UTF-8` | Support des caractères internationaux |
| `MARGIN` | `1` pixel | Marge fine pour lisibilité |
| `width/height` | `300x300` px | Taille optimale pour scan mobile |

**Niveaux de correction d'erreur :**

- **L (Low)** : 7% de correction
- **M (Medium)** : 15% de correction ⭐ **Utilisé**
- **Q (Quartile)** : 25% de correction
- **H (High)** : 30% de correction

---

### 4. **Conversion en Image PNG**

```java
/**
 * Convertit le QR code en tableau de bytes (PNG)
 * 
 * @param qrImage Image du QR code
 * @return Tableau de bytes de l'image PNG
 */
public byte[] convertQRCodeToBytes(BufferedImage qrImage) throws Exception {
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    
    // Écriture de l'image en format PNG
    // PNG est choisi car :
    // - Compression sans perte
    // - Support de la transparence
    // - Format standard pour le web
    ImageIO.write(qrImage, "PNG", outputStream);
    
    byte[] qrBytes = outputStream.toByteArray();
    outputStream.close();
    
    return qrBytes;
}
```

---

### 5. **Stockage de l'URL du QR Code**

```java
/**
 * Génère l'URL du QR code pour accès web
 * 
 * @param seanceId ID de la séance
 * @return URL du QR code
 */
public String generateQRCodeURL(int seanceId) {
    
    // Option 1 : URL de servlet locale
    String servletURL = "/api/seance/" + seanceId + "/qrcode";
    
    // Option 2 : URL avec token direct
    String tokenURL = "/validate/qr?token=" + qrToken;
    
    return tokenURL;
}
```

---

### 6. **Intégration avec la Séance**

```java
public class SeanceService {
    
    /**
     * Met à jour une séance avec un QR code généré
     */
    public void generateAndSaveQRCode(Seance seance) throws Exception {
        
        // 1. Générer le token unique
        String qrToken = UUID.randomUUID().toString();
        seance.setQrToken(qrToken);
        
        // 2. Calculer la date d'expiration
        LocalDateTime expiration = seance.getHeureFin().toLocalDateTime().plusMinutes(15);
        seance.setQrExpiresAt(Timestamp.valueOf(expiration));
        
        // 3. Générer le QR code
        BufferedImage qrImage = generateQRCode(qrToken, 300, 300);
        byte[] qrBytes = convertQRCodeToBytes(qrImage);
        
        // 4. Sauvegarder l'image (optionnel)
        String fileName = "qrcode_seance_" + seance.getId() + ".png";
        saveQRCodeToFile(qrBytes, fileName);
        
        // 5. Générer l'URL
        String qrURL = generateQRCodeURL(seance.getId());
        seance.setQrUrl(qrURL);
        
        // 6. Mettre à jour la séance en base de données
        this.edit(seance);
    }
}
```

---

## 📱 Scanner et Validation du QR Code

### 1. **API de Validation**

```java
/**
 * API REST pour valider un QR code scanné
 * 
 * @param token Token scanné depuis le QR code
 * @param studentId ID de l'étudiant qui scanne
 * @return Résultat de validation
 */
public Map<String, Object> validateQRCode(String token, int studentId) {
    
    Map<String, Object> result = new HashMap<>();
    
    try {
        // 1. Trouver la séance par token
        Seance seance = seanceService.getByQRToken(token);
        
        if (seance == null) {
            result.put("success", false);
            result.put("message", "QR code invalide");
            return result;
        }
        
        // 2. Vérifier la date d'expiration
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (seance.getQrExpiresAt().before(now)) {
            result.put("success", false);
            result.put("message", "QR code expiré");
            return result;
        }
        
        // 3. Vérifier si l'étudiant est déjà marqué présent
        if (presenceService.isPresent(studentId, seance.getId())) {
            result.put("success", false);
            result.put("message", "Présence déjà enregistrée");
            return result;
        }
        
        // 4. Enregistrer la présence
        presenceService.markPresent(studentId, seance.getId());
        
        result.put("success", true);
        result.put("message", "Présence enregistrée avec succès");
        result.put("seance", seance);
        
    } catch (Exception e) {
        result.put("success", false);
        result.put("message", "Erreur lors de la validation");
    }
    
    return result;
}
```

---

### 2. **Scanner avec Caméra Mobile**

```javascript
// Frontend : Utilisation de la bibliothèque html5-qrcode

import { Html5QrcodeScanner } from "html5-qrcode";

// Initialisation du scanner
const scanner = new Html5QrcodeScanner(
    "qr-reader",  // ID de l'élément DOM
    { fps: 10, qrbox: { width: 250, height: 250 } },
    /* verbose= */ false
);

// Callback lors de la détection d'un QR code
scanner.render(
    (decodedText, decodedResult) => {
        // decodedText contient le token du QR code
        handleQRCodeScan(decodedText);
    },
    (errorMessage) => {
        // Gestion des erreurs de scan
    }
);

// Fonction pour envoyer le token au serveur
async function handleQRCodeScan(token) {
    const response = await fetch('/api/presence/validate', {
        method: 'POST',
        body: JSON.stringify({ token: token, studentId: getCurrentStudentId() }),
        headers: { 'Content-Type': 'application/json' }
    });
    
    const result = await response.json();
    
    if (result.success) {
        alert("✅ " + result.message);
    } else {
        alert("❌ " + result.message);
    }
}
```

---

## 🔒 Sécurité

### 1. **Protection contre la réutilisation**

```java
/**
 * Vérifie si un QR code a déjà été scanné par un étudiant
 */
public boolean isQRCodeAlreadyUsed(String token, int studentId) {
    
    String sql = "SELECT COUNT(*) FROM presence " +
                "WHERE student_id = ? AND seance_id IN " +
                "(SELECT id FROM seance WHERE qr_token = ?)";
    
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setInt(1, studentId);
        ps.setString(2, token);
        
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
```

### 2. **Validation de la temporalité**

```java
/**
 * Vérifie que le QR code est utilisé dans la plage temporelle valide
 */
public boolean isValidTimeRange(Seance seance) {
    
    Timestamp now = new Timestamp(System.currentTimeMillis());
    Timestamp heureDebut = seance.getHeureDebut();
    Timestamp heureFin = seance.getHeureFin();
    Timestamp expiration = seance.getQrExpiresAt();
    
    // Le QR code doit être utilisé :
    // - Après le début de la séance
    // - Avant l'expiration (15 min après la fin)
    return now.after(heureDebut) && now.before(expiration);
}
```

---

## 📊 Statistiques et Rapports

### 1. **Taux de présence par QR code**

```java
/**
 * Calcule le taux de présence pour une séance
 */
public double calculateAttendanceRate(int seanceId) {
    
    // Nombre total d'étudiants dans la classe
    int totalStudents = studentService.countBySeance(seanceId);
    
    // Nombre de présences enregistrées par QR
    int qrPresences = presenceService.countBySeance(seanceId);
    
    if (totalStudents == 0) return 0.0;
    
    return (double) qrPresences / totalStudents * 100;
}
```

---

## 🎯 Cas d'Utilisation

### 1. **Création d'une séance**

1. Administrateur crée une séance
2. Système génère automatiquement un QR code
3. QR code est affiché dans l'interface enseignant
4. QR code est accessible via URL

### 2. **Marquage de présence**

1. Étudiant ouvre l'application mobile
2. Scanne le QR code affiché en classe
3. Système valide le token
4. Présence est enregistrée automatiquement

### 3. **Affichage des présences**

1. Enseignant consulte l'interface
2. Liste des étudiants présents via QR
3. Statistiques en temps réel
4. Export des données

---

## 🚀 Performance et Optimisation

### 1. **Caching des QR codes**

```java
// Utilisation de HashMap pour cache
private Map<Integer, BufferedImage> qrCodeCache = new HashMap<>();

public BufferedImage getCachedQRCode(int seanceId) throws Exception {
    
    // Vérifier si le QR code est en cache
    if (qrCodeCache.containsKey(seanceId)) {
        return qrCodeCache.get(seanceId);
    }
    
    // Générer et mettre en cache
    Seance seance = getById(seanceId);
    BufferedImage qrCode = generateQRCode(seance.getQrToken(), 300, 300);
    qrCodeCache.put(seanceId, qrCode);
    
    return qrCode;
}
```

### 2. **Compression d'images**

```java
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

/**
 * Compresse l'image du QR code pour réduire la taille
 */
public byte[] compressQRCode(BufferedImage qrImage) throws Exception {
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    // Configuration de compression JPEG
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
    ImageWriter writer = writers.next();
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.8f); // 80% de qualité
    
    // Écriture compressée
    ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
    writer.setOutput(ios);
    writer.write(null, new IIOImage(qrImage, null, null), param);
    
    return baos.toByteArray();
}
```

---

## 📝 Résumé des Points Clés

| Aspect | Détail |
|--------|--------|
| **Bibliothèque** | ZXing (Zebra Crossing) |
| **Format** | QR Code avec correction d'erreur Medium (15%) |
| **Token** | UUID unique pour chaque séance |
| **Expiration** | 15 minutes après la fin de la séance |
| **Stockage** | Base de données + fichiers PNG |
| **Validation** | Token + Date + État de présence |
| **Sécurité** | Protection contre réutilisation |
| **Performance** | Caching + Compression |

---

## 🔗 Ressources

- **ZXing GitHub** : https://github.com/zxing/zxing
- **QR Code Specification** : ISO/IEC 18004
- **Error Correction Levels** : Reed-Solomon algorithm

---

**Documentation générée pour EspritFlow - Gestion de Présence via QR Code** 🎓
