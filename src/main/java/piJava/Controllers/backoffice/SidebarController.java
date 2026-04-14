package piJava.Controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import piJava.entities.user;
import piJava.utils.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    // ─── Profile FXML fields ─────────────────────────────────────────────────
    @FXML private StackPane avatarStack;
    @FXML private Circle    avatarCircle;
    @FXML private ImageView avatarImage;
    @FXML private Label     avatarInitials;
    @FXML private Label     profileName;
    @FXML private Label     profilePrenom;
    @FXML private Label     profileNom;
    @FXML private Label     profileEmail;
    @FXML private Label     profileRole;
    @FXML private Label     roleBadgeLabel;
    @FXML private Label     sessionStatus;

    // ─── Badges ──────────────────────────────────────────────────────────────
    @FXML private Label tachesBadge;
    @FXML private Label notifBadge;

    // ─── Nav items ────────────────────────────────────────────────────────────
    @FXML private HBox dashboardBtn;
    @FXML private HBox utilisateursBtn;
    @FXML private HBox tachesBtn;
    @FXML private HBox classesBtn;
    @FXML private HBox matieresBtn;
    @FXML private HBox enseignantsBtn;
    @FXML private HBox emploiBtn;
    @FXML private HBox notesBtn;
    @FXML private HBox notificationsBtn;
    @FXML private HBox logoutBtn;

    // ─── Content area injected by MainController ──────────────────────────────
    // MainController calls setContentArea(contentArea) right after load
    private StackPane contentArea;

    // ─── Internal state ───────────────────────────────────────────────────────
    private HBox       activeButton = null;
    private List<HBox> allNavButtons;

    // ─── Initializable ────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allNavButtons = Arrays.asList(
                dashboardBtn, utilisateursBtn, tachesBtn, classesBtn,
                matieresBtn, enseignantsBtn, emploiBtn, notesBtn, notificationsBtn
        );
        bindSessionData();
        // Note: do NOT call goToDashboard() here — contentArea is null at this point.
        // MainController calls setContentArea() first, then goToDashboard().
    }

    // ─── Called by MainController after FXML injection ────────────────────────
    /**
     * Gives the sidebar a reference to the main content StackPane.
     * Must be called BEFORE goToDashboard() or any navigation.
     */
    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    // ─── Session data binding ─────────────────────────────────────────────────
    private void bindSessionData() {
        SessionManager session = SessionManager.getInstance();
        user u = session.getCurrentUser();
        if (u == null) return;

        profileName.setText(session.getFullName());
        profilePrenom.setText(nvl(u.getPrenom(), "—"));
        profileNom.setText(nvl(u.getNom(), "—"));
        profileEmail.setText(nvl(u.getEmail(), "—"));
        profileRole.setText(buildRoleDisplay(u.getRoles()));
        roleBadgeLabel.setText(session.isAdmin() ? "ADMINISTRATEUR" : "UTILISATEUR");
        avatarInitials.setText(buildInitials(u.getPrenom(), u.getNom()));
        loadProfilePicture(session.getProfilePic());

        String loginTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm"));
        sessionStatus.setText("En ligne · " + loginTime);
    }

    private void loadProfilePicture(String picPath) {
        if (picPath == null || picPath.isBlank()) {
            avatarImage.setVisible(false);
            avatarInitials.setVisible(true);
            return;
        }
        try {
            File f = new File(picPath);
            if (f.exists()) {
                Image img = new Image(new FileInputStream(f), 48, 48, true, true);
                avatarImage.setImage(img);
                Circle clip = new Circle(24, 24, 24);
                avatarImage.setClip(clip);
                avatarImage.setVisible(true);
                avatarInitials.setVisible(false);
            } else {
                avatarImage.setVisible(false);
                avatarInitials.setVisible(true);
            }
        } catch (Exception e) {
            avatarImage.setVisible(false);
            avatarInitials.setVisible(true);
        }
    }

    // ─── Active nav state ─────────────────────────────────────────────────────
    private void setActiveButton(HBox btn) {
        if (activeButton != null)
            activeButton.getStyleClass().remove("nav-item-active");
        if (btn != null && !btn.getStyleClass().contains("nav-item-active"))
            btn.getStyleClass().add("nav-item-active");
        activeButton = btn;
    }

    // ─── Navigation handlers (FXML + public so MainController can call them) ──
    @FXML
    public void goToDashboard() {
        setActiveButton(dashboardBtn);
        loadView("/backoffice/dashboard/dashboard-content.fxml");
    }

    @FXML
    public void goToUsers() {
        setActiveButton(utilisateursBtn);
        loadView("/backoffice/User/UserContent.fxml");
    }

    @FXML
    public void goToTaches() {
        setActiveButton(tachesBtn);
        loadView("/backoffice/taches/taches-content.fxml");
    }

    @FXML
    public void goToClasses() {
        setActiveButton(classesBtn);
        loadView("/backoffice/Classe/ClassesContent.fxml");
    }

    @FXML
    public void goToMatieres() {
        setActiveButton(matieresBtn);
        loadView("/backoffice/Matiere/MatiereContent.fxml");
    }

    @FXML
    public void goToEnseignants() {
        setActiveButton(enseignantsBtn);
        loadView("/piJava/Views/backoffice/enseignants/enseignants-content.fxml");
    }

    @FXML
    public void goToEmploi() {
        setActiveButton(emploiBtn);
        loadView("/piJava/Views/backoffice/emploi/emploi-content.fxml");
    }

    @FXML
    public void goToNotes() {
        setActiveButton(notesBtn);
        loadView("/backoffice/notes/notes-content.fxml");
    }

    @FXML
    public void goToNotifications() {
        setActiveButton(notificationsBtn);
        loadView("/backoffice/notifications/notif-content.fxml");
    }

    @FXML
    public void logout() {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Content loader ───────────────────────────────────────────────────────
    private void loadView(String fxmlPath) {
        if (contentArea == null) {
            System.err.println("SidebarController: contentArea is null — " +
                    "call setContentArea() before navigating.");
            return;
        }
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Public badge updaters ────────────────────────────────────────────────
    public void setTachesBadge(int count) {
        tachesBadge.setText(String.valueOf(count));
        tachesBadge.setVisible(count > 0);
    }

    public void setNotifBadge(int count) {
        notifBadge.setText(String.valueOf(count));
        notifBadge.setVisible(count > 0);
    }

    // ─── Utilities ────────────────────────────────────────────────────────────
    private static String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private static String buildInitials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isBlank()) ? prenom.substring(0, 1).toUpperCase() : "";
        String n = (nom    != null && !nom.isBlank())    ? nom.substring(0, 1).toUpperCase()    : "";
        return p + n;
    }

    private static String buildRoleDisplay(String roles) {
        if (roles == null || roles.isBlank()) return "Utilisateur";
        if (roles.contains("ROLE_ADMIN"))      return "Administrateur";
        if (roles.contains("ROLE_ENSEIGNANT")) return "Enseignant";
        if (roles.contains("ROLE_ETUDIANT"))   return "Étudiant";
        return "Utilisateur";
    }
}