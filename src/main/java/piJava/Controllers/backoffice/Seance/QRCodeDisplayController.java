package piJava.Controllers.backoffice.Seance;

// ============================================================================
// IMPORTATIONS DES BIBLIOTHÈQUES
// ============================================================================

// ZXing : librairie pour générer des codes QR
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

// JavaFX Integration : permet de convertir des images Swing en images JavaFX
import javafx.embed.swing.SwingFXUtils;

// JavaFX FXML : annotations et chargement des fichiers FXML
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

// Parent et Scene : éléments de base de la scène JavaFX
import javafx.scene.Parent;
import javafx.scene.Scene;

// Contrôles UI : Alert (messages), Button, Label
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

// Images : Image et ImageView pour afficher les images
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Stages : gestion des fenêtres
import javafx.stage.Modality;
import javafx.stage.Stage;

// Entités : la classe Seance représenter une séance
import piJava.entities.Seance;

// Services : interfaces d'accès aux données
import piJava.services.ClasseService;
import piJava.services.MatiereService;
import piJava.services.SalleService;
import piJava.services.SeanceService;

// ============================================================================
// AUTRES IMPORTATIONS JAVA
// ============================================================================

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Controller pour l'affichage du QR Code de présence d'une séance
 * Ce controller permet de Générer, Afficher et Gérer les présences via QR Code
 */
public class QRCodeDisplayController {

    // ============================================================================
    // ANNOTATIONS FXML - Ces champs sont injectés depuis le fichier FXML
    // ============================================================================

    // Labels d'affichage des informations de la séance
    @FXML private Label lblMatiere;      // Nom de la matière
    @FXML private Label lblClasse;       // Nom de la classe
    @FXML private Label lblSalle;       // Nom de la salle
    @FXML private Label lblDate;        // Date de la séance
    @FXML private Label lblDebut;       // Heure de début
    @FXML private Label lblFin;         // Heure de fin
    
    // ImageView : zone d'affichage du QR Code généré
    @FXML private ImageView qrImageView;

    // Boutons d'action
    @FXML private Button btnRetour;         // Retour à la fenêtre précédente
    @FXML private Button btnImprimer;        // Imprimer le QR Code
    @FXML private Button btnVoirPresences; // Voir les présences de la séance

    // ============================================================================
    // VARIABLES D'INSTANCE
    // ============================================================================

    // Seance courante : l'objet Seance dont on affiche les infos
    private Seance currentSeance;

    // Services pour récupérer les données depuis la base de données
    private SeanceService seanceService = new SeanceService();
    private MatiereService matiereService = new MatiereService();
    private ClasseService classeService = new ClasseService();
    private SalleService salleService = new SalleService();

    // ============================================================================
    // MÉTHODES PUBLIQUES
    // ============================================================================

    /**
     * Initialise les données du controller avec une séance
     * @param seance la séance à afficher
     */
    public void initData(Seance seance) {
        this.currentSeance = seance;  // Stocker la séance sélectionnée
        loadDetails();                 // Charger les détails (matiere, classe, salle...)
        generateAndDisplayQR();        // Générer et afficher le QR Code
    }

    // ============================================================================
    // MÉTHODES PRIVÉES - CHARGEMENT DES DONNÉES
    // ============================================================================

    /**
     * Charge les détails de la séance et met à jour les labels
     * Recherche les noms de la matière, classe et salle à partir de leurs IDs
     */
    private void loadDetails() {
        try {
            // ----------------------------------------------------------------
            // Recherche du nom de la matière correspondant à matiereId
            // ----------------------------------------------------------------
            // Étape 1: Récupérer toutes les matières avec matiereService.show()
            // Étape 2: Filtrer pour trouver celle dont l'ID correspond à matiereId de la séance
            // Étape 3: Extraire le nom avec .map(m -> m.getNom())
            // Étape 4: Si non trouvé, utiliser "N/A" par défaut
            String matiere = matiereService.show().stream()
                .filter(m -> m.getId() == currentSeance.getMatiereId())
                .map(m -> m.getNom())
                .findFirst().orElse("N/A");

            // ----------------------------------------------------------------
            // Recherche du nom de la classe correspondant à classeId
            // ----------------------------------------------------------------
            String classe = classeService.getAllClasses().stream()
                .filter(c -> c.getId() == currentSeance.getClasseId())
                .map(c -> c.getNom())
                .findFirst().orElse("N/A");

            // ----------------------------------------------------------------
            // Recherche du nom de la salle correspondant à salleId
            // ----------------------------------------------------------------
            String salle = salleService.getAllSalles().stream()
                .filter(s -> s.getId() == currentSeance.getSalleId())
                .map(s -> s.getName())
                .findFirst().orElse("N/A");

            // Mise à jour des labels avec les informations récupérées
            lblMatiere.setText(matiere);
            lblClasse.setText(classe);
            lblSalle.setText(salle);

            // ----------------------------------------------------------------
            // Gestion des heures de début et fin
            // ----------------------------------------------------------------
            if (currentSeance.getHeureDebut() != null && currentSeance.getHeureFin() != null) {
                // Convertir les timestamps SQL en LocalDateTime pour manipulation
                LocalDateTime debut = currentSeance.getHeureDebut().toLocalDateTime();
                LocalDateTime fin = currentSeance.getHeureFin().toLocalDateTime();

                // Formatter les dates/heures au format "dd/MM/yyyy" et "HH:mm"
                lblDate.setText(debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                lblDebut.setText(debut.format(DateTimeFormatter.ofPattern("HH:mm")));
                lblFin.setText(fin.format(DateTimeFormatter.ofPattern("HH:mm")));
            } else {
                // Si les heures ne sont pas définies, afficher "N/A"
                lblDate.setText("N/A");
                lblDebut.setText("N/A");
                lblFin.setText("N/A");
            }
        } catch (SQLException e) {
            // Gestion des erreurs de base de données
            e.printStackTrace();
        }
    }

    /**
     * Génère le QR Code et l'affiche dans l'ImageView
     * Le QR Code contient "ID:TOKEN" pour valider la présence
     */
    private void generateAndDisplayQR() {
        try {
            // ----------------------------------------------------------------
            // Génération d'un token unique si inexistant
            // ----------------------------------------------------------------
            // Si la séance n'a pas encore de token QR, on en génère un nouveau
            if (currentSeance.getQrToken() == null || currentSeance.getQrToken().isEmpty()) {
                // UUID.randomUUID() génère un identifiant unique universel
                String token = UUID.randomUUID().toString();
                currentSeance.setQrToken(token);
                
                // Définir l'expiration : 1 heure après la fin de la séance
                if (currentSeance.getHeureFin() != null) {
                    LocalDateTime end = currentSeance.getHeureFin().toLocalDateTime().plusHours(1);
                    currentSeance.setQrExpiresAt(Timestamp.valueOf(end));
                }
                
                // Sauvegarder le nouveau token dans la base de données
                seanceService.edit(currentSeance);
            }

            // ----------------------------------------------------------------
            // Contenu du QR Code
            // ----------------------------------------------------------------
            // Format: "SEANCE_ID:TOKEN" - Cet identifiant permet de valider le scan
            String qrContent = currentSeance.getId() + ":" + currentSeance.getQrToken();

            // ----------------------------------------------------------------
            // Génération de la matrice du QR Code avec ZXing
            // ----------------------------------------------------------------
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Encoder le contenu en QR Code 300x300 pixels
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            // ----------------------------------------------------------------
            // Conversion de la BitMatrix en BufferedImage
            // ----------------------------------------------------------------
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            
            // Créer une image RGB de même dimension
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            // Parcourir chaque pixel pour définir la couleur (noir ou blanc)
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // bitMatrix.get(x, y) retourne true pour un pixel noir, false pour blanc
                    // 0xFF000000 = noir (0xAARRGGBB), 0xFFFFFFFF = blanc
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // ----------------------------------------------------------------
            // Conversion et affichage de l'image
            // ----------------------------------------------------------------
            // SwingFXUtils permet de convertir une image Swing en image JavaFX
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            qrImageView.setImage(fxImage); // Afficher dans l'ImageView

        } catch (Exception e) {
            // Gérer les erreurs de génération QR
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur QR", "Erreur lors de la génération du QR Code.");
        }
    }

    // ============================================================================
    // GESTIONNAIRES D'ÉVÉNEMENTS (Event Handlers)
    // ============================================================================

    /**
     * Gère le clic sur le bouton "Retour"
     * Ferme la fenêtre actuelle
     */
    @FXML
    private void handleRetour() {
        // Récupérer la fenêtre actuelle à partir du bouton
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.close(); // Fermer la fenêtre
    }

    /**
     * Gère le clic sur le bouton "Imprimer"
     * Affiche un message (fonctionnalité à implémenter)
     */
    @FXML
    private void handleImprimer() {
        showAlert(Alert.AlertType.INFORMATION, "Impression", "Fonctionnalité d'impression à venir.");
    }

    /**
     * Gère le clic sur le bouton "Voir Présences"
     * Ouvre le dashboard des présences pour cette séance
     */
    @FXML
    private void handleVoirPresences() {
        try {
            // Charger le fichier FXML du dashboard des présences
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/Seance/SeanceDashboard.fxml"));
            Parent root = loader.load();
            
            // Initialiser le controller avec la séance actuelle
            SeanceDashboardController controller = loader.getController();
            controller.initData(currentSeance);
            
            // Ouvrir une nouvelle fenêtre/scène
            Stage stage = (Stage) btnVoirPresences.getScene().getWindow();
            stage.setTitle("Dashboard Présence - Séance #" + currentSeance.getId());
            stage.setScene(new Scene(root));
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le Dashboard.");
        }
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    /**
     * Affiche une boîte de dialogue d'alerte
     * @param type Type d'alerte (INFORMATION, WARNING, ERROR)
     * @param title Titre de la fenêtre
     * @param content Contenu du message
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Pas de sous-titre
        alert.setContentText(content);
        
        // ----------------------------------------------------------------
        // Style personnalisé de l'alerte (thème sombre)
        // ----------------------------------------------------------------
        alert.getDialogPane().setStyle(
            "-fx-background-color: #111318; " +      // Fond sombre
            "-fx-border-color: #1e2130; " +      // Bordure grise foncé
            "-fx-border-width: 1;"              // Épaisseur de la bordure
        );
        
        // Modifier la couleur du texte du contenu
        Label label = (Label) alert.getDialogPane().lookup(".content.label");
        if(label != null) label.setStyle("-fx-text-fill: white;");
        
        // Afficher et attendre que l'utilisateur ferme
        alert.showAndWait();
    }
}
