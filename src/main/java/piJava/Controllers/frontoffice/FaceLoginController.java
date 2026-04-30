package piJava.Controllers.frontoffice;

import nu.pattern.OpenCV;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import piJava.entities.user;
import piJava.services.FaceRecognitionService;
import piJava.services.FaceRecognitionService.FaceRecognitionResult;
import piJava.services.UserServices;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JavaFX controller for a face-recognition login screen.
 *
 * Flow:
 *  1. User clicks "Se connecter par visage"
 *  2. Camera preview opens (live feed via OpenCV)
 *  3. User clicks "Capturer" – one frame is sent to the Python API
 *  4. On success the matched user is loaded from the DB and the session is started
 *
 * FXML requirements:
 *   fx:id="cameraPreview"       – ImageView for live feed
 *   fx:id="startCameraBtn"      – Button to open camera
 *   fx:id="captureBtn"          – Button to capture & recognize
 *   fx:id="stopCameraBtn"       – Button to close camera
 *   fx:id="statusLabel"         – Label for feedback messages
 *   fx:id="loadingSpinner"      – VBox/StackPane shown while API call is in progress
 *   fx:id="resultBox"           – VBox shown after recognition
 *   fx:id="resultNameLabel"     – Label showing matched name
 *   fx:id="resultEmailLabel"    – Label showing matched email
 */
public class FaceLoginController implements Initializable {

    private static volatile boolean opencvLoaded = false;

    // ── FXML ──────────────────────────────────────────────────
    @FXML private ImageView  cameraPreview;
    @FXML private Button     startCameraBtn;
    @FXML private Button     captureBtn;
    @FXML private Button     stopCameraBtn;
    @FXML private Label      statusLabel;
    @FXML private StackPane  loadingSpinner;
    @FXML private VBox       resultBox;
    @FXML private Label      resultNameLabel;
    @FXML private Label      resultEmailLabel;

    // ── Services ──────────────────────────────────────────────
    private final FaceRecognitionService faceService = new FaceRecognitionService();
    private final UserServices           userServices = new UserServices();

    // ── Camera state ──────────────────────────────────────────
    private VideoCapture            camera;
    private ScheduledExecutorService cameraTimer;
    private volatile Mat            latestFrame;         // holds the most recent camera frame
    private volatile boolean        cameraRunning = false;
    
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    // ── Lifecycle ─────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ensureOpenCvLoaded();
        setCaptureEnabled(false);
        setLoading(false);
        resultBox.setVisible(false);
        statusLabel.setText("Cliquez sur « Ouvrir la caméra » pour commencer.");

        // Check if the Python server is reachable
        Task<Boolean> healthCheck = new Task<>() {
            @Override protected Boolean call() { return faceService.isHealthy(); }
        };
        healthCheck.setOnSucceeded(e -> {
            if (!healthCheck.getValue()) {
                setStatus("⚠️ Serveur Python introuvable. Démarrez face_recognition_api.py d'abord.", "error");
                startCameraBtn.setDisable(true);
            }
        });
        new Thread(healthCheck, "face-health-check").start();
    }

    // ── Camera controls ────────────────────────────────────────

    @FXML
    private void handleStartCamera() {
        // OpenCV must be loaded: System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            setStatus("❌ Impossible d'accéder à la caméra.", "error");
            return;
        }

        cameraRunning = true;
        latestFrame   = new Mat();

        // Capture a frame every ~33 ms → ~30 fps preview
        cameraTimer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "camera-feed");
            t.setDaemon(true);
            return t;
        });
        cameraTimer.scheduleAtFixedRate(this::grabFrame, 0, 33, TimeUnit.MILLISECONDS);

        startCameraBtn.setDisable(true);
        stopCameraBtn.setDisable(false);
        setCaptureEnabled(true);
        resultBox.setVisible(false);
        setStatus("📷 Caméra active. Positionnez votre visage puis cliquez « Capturer ».", "info");
    }

    @FXML
    private void handleStopCamera() {
        stopCamera();
        setStatus("Caméra arrêtée.", "info");
    }

    @FXML
    private void handleBackToLogin() {
        shutdown();
        navigateTo("/login.fxml");
    }

    @FXML
    private void handleCapture() {
        if (!cameraRunning || latestFrame == null || latestFrame.empty()) {
            setStatus("⚠️ Aucune image disponible.", "error");
            return;
        }

        // Encode the latest frame to JPEG base64
        String base64;
        try {
            MatOfByte buf = new MatOfByte();
            Imgcodecs.imencode(".jpg", latestFrame, buf);
            byte[] bytes = buf.toArray();
            base64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            setStatus("❌ Erreur d'encodage image: " + ex.getMessage(), "error");
            return;
        }

        // Stop the live preview and send to API in a background thread
        stopCamera();
        setLoading(true);
        setCaptureEnabled(false);
        setStatus("🔍 Analyse du visage en cours…", "info");

        Task<FaceRecognitionResult> task = new Task<>() {
            @Override protected FaceRecognitionResult call() throws Exception {
                return faceService.recognizeFace(base64);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            handleRecognitionResult(task.getValue());
        });

        task.setOnFailed(e -> {
            setLoading(false);
            String msg = task.getException() != null
                         ? task.getException().getMessage()
                         : "Erreur inconnue";
            setStatus("❌ Échec de la communication avec le serveur: " + msg, "error");
            startCameraBtn.setDisable(false);
        });

        Thread t = new Thread(task, "face-api-call");
        t.setDaemon(true);
        t.start();
    }

    // ── Recognition result handler ─────────────────────────────

    private void handleRecognitionResult(FaceRecognitionResult result) {
        System.out.println("Face recognition result: " + result);

        boolean isFailure = (result.status == FaceRecognitionResult.Status.UNKNOWN ||
                             result.status == FaceRecognitionResult.Status.NO_USER ||
                             result.status == FaceRecognitionResult.Status.ERROR);

        if (isFailure) {
            failedAttempts++;
        } else if (result.status == FaceRecognitionResult.Status.SUCCESS) {
            failedAttempts = 0;
        }

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            setStatus("⚠️ Trop d'échecs (" + failedAttempts + "). Redirection vers connexion...", "error");
            startCameraBtn.setDisable(true);
            
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            delay.setOnFinished(e -> navigateTo("/login.fxml"));
            delay.play();
            return;
        }

        switch (result.status) {
            case SUCCESS -> {
                // Load the full user object from DB using the returned userId
                user matched = userServices.getById(result.userId);
                if (matched != null) {
                    onLoginSuccess(matched, result);
                } else {
                    setStatus("⚠️ Visage reconnu mais utilisateur introuvable en base.", "error");
                    startCameraBtn.setDisable(false);
                }
            }
            case UNKNOWN -> {
                setStatus("❓ Visage non reconnu. Veuillez vous connecter manuellement.", "warning");
                showResult("Inconnu", "", false);
            }
            case BANNED -> {
                setStatus("🚫 Ce compte est banni et ne peut pas se connecter.", "error");
                showResult(result.fullName, result.email, false);
            }
            case UNVERIFIED -> {
                setStatus("📧 Compte non vérifié. Vérifiez votre email.", "warning");
                showResult(result.fullName, result.email, false);
            }
            case NO_USER -> {
                setStatus("⚠️ Visage détecté mais aucun compte associé en base.", "warning");
                startCameraBtn.setDisable(false);
            }
            case ERROR -> {
                setStatus("❌ Erreur: " + result.message, "error");
                startCameraBtn.setDisable(false);
            }
        }
    }

    private void onLoginSuccess(user u, FaceRecognitionResult result) {
        showResult(u.getPrenom() + " " + u.getNom(), u.getEmail(), true);
        setStatus(String.format("✅ Bienvenue, %s ! (confiance: %.0f%%)",
                  u.getPrenom(), (1 - result.distance) * 100), "success");

        piJava.utils.SessionManager.getInstance().login(u);

        System.out.println("Logged in via face recognition: " + u);

        Platform.runLater(() -> {
            if (piJava.utils.SessionManager.getInstance().isAdmin()) {
                navigateTo("/backoffice/main.fxml");
            } else {
                navigateTo("/frontoffice/main.fxml");
            }
        });
    }

    private void navigateTo(String fxmlPath) {
        try {
            stopCamera();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) startCameraBtn.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("Navigation Error: " + e.getMessage());
            setStatus("❌ Erreur de navigation.", "error");
        }
    }

    // ── Camera internals ──────────────────────────────────────

    private void grabFrame() {
        if (!cameraRunning || camera == null || !camera.isOpened()) return;
        Mat frame = new Mat();
        if (camera.read(frame) && !frame.empty()) {
            latestFrame = frame;
            Image fxImage = matToFxImage(frame);
            Platform.runLater(() -> cameraPreview.setImage(fxImage));
        }
    }

    private void stopCamera() {
        cameraRunning = false;
        if (cameraTimer != null && !cameraTimer.isShutdown()) {
            cameraTimer.shutdownNow();
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        startCameraBtn.setDisable(false);
        stopCameraBtn.setDisable(true);
        setCaptureEnabled(false);
    }

    /** Convert an OpenCV Mat (BGR) to a JavaFX Image. */
    private Image matToFxImage(Mat mat) {
        MatOfByte buf = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, buf);
        return new Image(new ByteArrayInputStream(buf.toArray()));
    }

    // ── UI helpers ────────────────────────────────────────────

    private void setLoading(boolean show) {
        Platform.runLater(() -> {
            loadingSpinner.setVisible(show);
            loadingSpinner.setManaged(show);
        });
    }

    private void setCaptureEnabled(boolean enabled) {
        Platform.runLater(() -> captureBtn.setDisable(!enabled));
    }

    private void setStatus(String text, String styleClass) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            statusLabel.getStyleClass().removeAll("status-info", "status-success", "status-error", "status-warning");
            statusLabel.getStyleClass().add("status-" + styleClass);
        });
    }

    private void showResult(String name, String email, boolean success) {
        Platform.runLater(() -> {
            resultNameLabel.setText(name);
            resultEmailLabel.setText(email);
            resultBox.setVisible(true);
            resultBox.getStyleClass().removeAll("result-success", "result-failure");
            resultBox.getStyleClass().add(success ? "result-success" : "result-failure");
            if (!success) startCameraBtn.setDisable(false);
        });
    }

    /** Clean up camera resources when the view is closed. */
    public void shutdown() {
        stopCamera();
    }

    private void ensureOpenCvLoaded() {
        if (opencvLoaded) {
            return;
        }
        synchronized (FaceLoginController.class) {
            if (opencvLoaded) {
                return;
            }
            try {
                OpenCV.loadLocally();
                opencvLoaded = true;
            } catch (Throwable t) {
                setStatus("Erreur de chargement OpenCV: " + t.getMessage(), "error");
                startCameraBtn.setDisable(true);
            }
        }
    }
}
