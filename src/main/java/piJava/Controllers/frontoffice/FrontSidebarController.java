package piJava.Controllers.frontoffice;

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

public class FrontSidebarController implements Initializable {

    // ─── Profile FXML fields ─────────────────────────────────────────────────
    @FXML private StackPane avatarStack;
    @FXML private Circle    avatarCircle;
    @FXML private ImageView avatarImage;
    @FXML private Label     avatarInitials;
    @FXML private Label     lblProfileName;
    @FXML private Label     profilePrenom;
    @FXML private Label     profileNom;
    @FXML private Label     profileEmail;
    @FXML private Label     lblProfileRole;
    @FXML private Label     roleBadgeLabel;
    @FXML private Label     sessionStatus;

    // ─── Badge ───────────────────────────────────────────────────────────────
    @FXML private Label notifBadge;

    // ─── Nav items ────────────────────────────────────────────────────────────
    @FXML private HBox dashboardBtn;
    @FXML private HBox tachesBtn;
    @FXML private HBox classesBtn;
    @FXML private HBox matieresBtn;
    @FXML private HBox enseignantsBtn;
    @FXML private HBox emploiBtn;
    @FXML private HBox ObjectifsSante;
    @FXML private HBox notificationsBtn;
    @FXML private HBox logoutBtn;

    // ─── Content area — injected by the parent layout controller ─────────────
    private StackPane contentArea;

    // ─── Internal state ───────────────────────────────────────────────────────
    private HBox       activeButton;
    private List<HBox> allNavButtons;

    private static final String UPLOAD_DIR =
            "C:\\Users\\MSI\\Documents\\my_project_dev\\public\\uploads\\profile_pics\\";

    // ─── Initializable ────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allNavButtons = Arrays.asList(
                dashboardBtn, tachesBtn, classesBtn, matieresBtn,
                enseignantsBtn, emploiBtn, ObjectifsSante, notificationsBtn
        );
        bindSessionData();
        // contentArea is null here — navigation is triggered after setContentArea()
    }

    // ─── Called by parent layout controller after FXML injection ─────────────
    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    // ─── Session data binding ─────────────────────────────────────────────────
    private void bindSessionData() {
        SessionManager session = SessionManager.getInstance();
        user u = session.getCurrentUser();
        if (u == null) return;

        lblProfileName.setText(session.getFullName());
        profilePrenom.setText(nvl(u.getPrenom(), "—"));
        profileNom.setText(nvl(u.getNom(), "—"));
        profileEmail.setText(nvl(u.getEmail(), "—"));

        String roleDisplay = buildRoleDisplay(u.getRoles());
        lblProfileRole.setText(roleDisplay);
        roleBadgeLabel.setText(buildRoleBadge(u.getRoles()));

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
            File f = new File(UPLOAD_DIR + picPath);
            if (!f.exists()) f = new File(picPath); // fallback: treat as absolute path
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
            activeButton.getStyleClass().remove("f-nav-item-active");
        if (btn != null && !btn.getStyleClass().contains("f-nav-item-active"))
            btn.getStyleClass().add("f-nav-item-active");
        activeButton = btn;
    }

    // ─── Navigation handlers ──────────────────────────────────────────────────
    @FXML
    public void goToDashboard() {
        setActiveButton(dashboardBtn);
        loadView("/frontoffice/dashboard/dashboard-content.fxml");
    }

    @FXML
    public void goToTaches() {
        setActiveButton(tachesBtn);
        loadView("/frontoffice/taches/taches-content.fxml");
    }

    @FXML
    public void goToClasses() {
        setActiveButton(classesBtn);
        loadView("/piJava/Views/frontoffice/classes/classes-content.fxml");
    }

    @FXML
    public void goToMatieres() {
        setActiveButton(matieresBtn);
        loadView("/piJava/Views/frontoffice/matieres/matieres-content.fxml");
    }

    @FXML
    public void goToEnseignants() {
        setActiveButton(enseignantsBtn);
        loadView("/piJava/Views/frontoffice/enseignants/enseignants-content.fxml");
    }

    @FXML
    public void goToEmploi() {
        setActiveButton(emploiBtn);
        loadView("/piJava/Views/frontoffice/emploi/emploi-content.fxml");
    }

    @FXML
    public void goToObjectifsSante() {
        setActiveButton(ObjectifsSante);
        loadView("/frontoffice/objectifsante/AfficherObjectifs.fxml");
    }

    @FXML
    public void goToObjectifs() {
        goToObjectifsSante();
    }

    @FXML
    public void goToNotifications() {
        setActiveButton(notificationsBtn);
        loadView("/frontoffice/notifications/notif-content.fxml");
    }
    public void goToProfile(){
        loadView("/frontoffice/user/profile.fxml");
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
            System.err.println("FrontSidebarController: contentArea is null — call setContentArea() before navigating.");
            return;
        }

        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            Object controller = loader.getController();

            // Injection pour Objectifs santé
            if (controller instanceof piJava.Controllers.frontoffice.objectifsante.AfficherObjectifsController c) {
                c.setSidebarController(this);
                c.setContentArea(contentArea);
            }



            // Injection pour Suivis bien-être
            if (controller instanceof piJava.Controllers.frontoffice.suivibienetre.AfficherSuivisController c) {
                c.setSidebarController(this);
                c.setContentArea(contentArea);
            }



            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Public badge updater ─────────────────────────────────────────────────
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
        if (roles.contains("ROLE_PROF"))       return "Professeur";
        return "Étudiant";
    }

    private static String buildRoleBadge(String roles) {
        if (roles == null || roles.isBlank()) return "ÉTUDIANT";
        if (roles.contains("ROLE_ADMIN"))      return "ADMINISTRATEUR";
        if (roles.contains("ROLE_ENSEIGNANT")) return "ENSEIGNANT";
        if (roles.contains("ROLE_PROF"))       return "PROFESSEUR";
        return "ÉTUDIANT";
    }
}
