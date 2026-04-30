package piJava.Controllers.frontoffice.emploi;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import piJava.entities.Attendance;
import piJava.entities.Seance;
import piJava.entities.user;
import piJava.services.AttendanceService;
import piJava.services.SeanceService;
import piJava.utils.SessionManager;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ScanQRCodeController implements Initializable {

    @FXML private ImageView cameraView;
    @FXML private Button btnConfirmer;
    @FXML private Label lblStatus;
    @FXML private Rectangle overlayRect;

    private Webcam webcam = null;
    private boolean isScanning = false;
    private Task<Void> webCamTask;
    
    private SeanceService seanceService = new SeanceService();
    private AttendanceService attendanceService = new AttendanceService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startCamera();
    }

    private void startCamera() {
        webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            isScanning = true;

            webCamTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    while (isScanning) {
                        if (webcam.isOpen()) {
                            if ((webcam.getImage()) != null) {
                                BufferedImage image = webcam.getImage();
                                
                                // Update UI with camera feed
                                Platform.runLater(() -> {
                                    Image fxImage = SwingFXUtils.toFXImage(image, null);
                                    cameraView.setImage(fxImage);
                                });

                                // Try decoding QR Code
                                try {
                                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
                                    Result result = new MultiFormatReader().decode(bitmap);
                                    
                                    if (result != null) {
                                        isScanning = false; // Stop scanning
                                        String qrContent = result.getText();
                                        Platform.runLater(() -> handleQrResult(qrContent));
                                    }
                                } catch (NotFoundException e) {
                                    // No QR code found in this frame, ignore
                                }
                            }
                        }
                        try {
                            Thread.sleep(100); // Check 10 times a second
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            };
            
            Thread webCamThread = new Thread(webCamTask);
            webCamThread.setDaemon(true);
            webCamThread.start();
        } else {
            lblStatus.setText("Aucune caméra trouvée !");
        }
    }

    private void handleQrResult(String qrContent) {
        // Stop the webcam when handled
        stopCamera();
        
        try {
            // qrContent format: "SEANCE_ID:TOKEN"
            String[] parts = qrContent.split(":");
            if (parts.length != 2) {
                showResult("QR Code Invalide", "Ce QR Code n'est pas reconnu par le système.", "error");
                return;
            }
            
            int seanceId = Integer.parseInt(parts[0]);
            String token = parts[1];
            
            Seance seance = seanceService.getById(seanceId);
            if (seance == null || !token.equals(seance.getQrToken())) {
                showResult("QR Code Invalide", "Ce QR Code n'est pas valide ou a expiré.", "error");
                return;
            }
            
            // Check Expiration (Optionnel: si la séance a une date d'expiration stricte)
            if (seance.getQrExpiresAt() != null && LocalDateTime.now().isAfter(seance.getQrExpiresAt().toLocalDateTime())) {
                showResult("QR Code Expiré", "La période de scan pour cette séance est terminée.", "error");
                return;
            }
            
            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showResult("Non Connecté", "Vous devez être connecté pour marquer votre présence.", "error");
                return;
            }
            
            // Verifier si deja present
            Attendance exist = attendanceService.getBySeanceAndUser(seanceId, currentUser.getId());
            if (exist != null) {
                showResult("Déjà Scanné", "Votre présence a déjà été enregistrée (Statut: " + exist.getStatus() + ").", "warning");
                return;
            }
            
            // Determiner Statut (PRESENT, RETARD, ABSENT)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime debut = seance.getHeureDebut().toLocalDateTime();
            LocalDateTime fin = seance.getHeureFin().toLocalDateTime();
            
            String status = "ABSENT";
            
            if (now.isBefore(fin)) {
                long minutesDiff = ChronoUnit.MINUTES.between(debut, now);
                if (minutesDiff <= 30) {
                    status = (minutesDiff <= 0) ? "PRESENT" : "RETARD";
                } else {
                    status = "ABSENT"; // Apres 30 mins, c'est consideré comme absent
                }
            } else {
                status = "ABSENT"; // Séance terminée
            }
            
            // Enregistrer
            Attendance att = new Attendance(seanceId, currentUser.getId(), status, Timestamp.valueOf(now));
            attendanceService.add(att);
            
            if (status.equals("PRESENT")) {
                showResult("Présence Marquée !", "Vous avez été marqué comme Présent.", "success");
            } else if (status.equals("RETARD")) {
                showResult("En Retard", "Votre présence est marquée avec un retard.", "warning");
            } else {
                showResult("Absent", "Délai dépassé, vous êtes marqué comme Absent.", "error");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showResult("Erreur", "Une erreur est survenue lors du traitement.", "error");
        }
    }

    private void showResult(String title, String message, String type) {
        lblStatus.setText(title + " - " + message);
        
        Alert.AlertType alertType = Alert.AlertType.INFORMATION;
        if (type.equals("success")) {
            overlayRect.setStyle("-fx-stroke: #10b981;"); // Green
            alertType = Alert.AlertType.INFORMATION;
        } else if (type.equals("warning")) {
            overlayRect.setStyle("-fx-stroke: #f59e0b;"); // Yellow/Orange
            alertType = Alert.AlertType.WARNING;
        } else {
            overlayRect.setStyle("-fx-stroke: #ef4444;"); // Red
            alertType = Alert.AlertType.ERROR;
        }
        
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        btnConfirmer.setText("Fermer");
        btnConfirmer.setOnAction(e -> {
            Stage stage = (Stage) btnConfirmer.getScene().getWindow();
            stage.close();
        });
    }

    private void stopCamera() {
        isScanning = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    @FXML
    private void handleConfirmer() {
        stopCamera();
        Stage stage = (Stage) btnConfirmer.getScene().getWindow();
        stage.close();
    }
}
