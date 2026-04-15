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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML private StackPane avatarStack;
    @FXML private Circle avatarCircle;
    @FXML private ImageView avatarImage;
    @FXML private Label avatarInitials;
    @FXML private Label profileName;
    @FXML private Label profilePrenom;
    @FXML private Label profileNom;
    @FXML private Label profileEmail;
    @FXML private Label profileRole;
    @FXML private Label roleBadgeLabel;
    @FXML private Label sessionStatus;

    @FXML private Label tachesBadge;
    @FXML private Label notifBadge;

    @FXML private HBox dashboardBtn;
    @FXML private HBox utilisateursBtn;
    @FXML private HBox tachesBtn;
    @FXML private HBox classesBtn;
    @FXML private HBox groupesBtn;
    @FXML private HBox matieresBtn;
    @FXML private HBox enseignantsBtn;
    @FXML private HBox emploiBtn;
    @FXML private HBox objectfiSanteBtn;
    @FXML private HBox notificationsBtn;
    @FXML private HBox logoutBtn;

    private StackPane contentArea;
    private HBox activeButton = null;
    private List<HBox> allNavButtons;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allNavButtons = Arrays.asList(
                dashboardBtn, utilisateursBtn, tachesBtn, classesBtn, groupesBtn,
                matieresBtn, enseignantsBtn, emploiBtn, objectfiSanteBtn, notificationsBtn
        );
        bindSessionData();
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    private void bindSessionData() {
        Object session = getSessionManager();
        Object user = invokeNoArg(session, "getCurrentUser");
        if (user == null) {
            return;
        }

        profileName.setText(stringValue(invokeNoArg(session, "getFullName"), ""));
        profilePrenom.setText(nvl(stringValue(invokeNoArg(user, "getPrenom"), null), "-"));
        profileNom.setText(nvl(stringValue(invokeNoArg(user, "getNom"), null), "-"));
        profileEmail.setText(nvl(stringValue(invokeNoArg(user, "getEmail"), null), "-"));

        String roles = stringValue(invokeNoArg(user, "getRoles"), "");
        profileRole.setText(buildRoleDisplay(roles));
        roleBadgeLabel.setText(booleanValue(invokeNoArg(session, "isAdmin")) ? "ADMINISTRATEUR" : "UTILISATEUR");
        avatarInitials.setText(buildInitials(
                stringValue(invokeNoArg(user, "getPrenom"), ""),
                stringValue(invokeNoArg(user, "getNom"), "")
        ));
        loadProfilePicture(stringValue(invokeNoArg(session, "getProfilePic"), null));

        String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
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

    private void setActiveButton(HBox btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-item-active");
        }
        if (btn != null && !btn.getStyleClass().contains("nav-item-active")) {
            btn.getStyleClass().add("nav-item-active");
        }
        activeButton = btn;
    }

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
    public void goToGroupes() {
        setActiveButton(groupesBtn);
        loadView("/backoffice/group/GroupContent.fxml");
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
    public void goToObjectifsSante() {
        setActiveButton(objectfiSanteBtn);
        loadView("/backoffice/objectifsante/AfficherObjectifs.fxml");
    }

    @FXML
    public void goToObjectifs() {
        goToObjectifsSante();
    }

    @FXML
    public void goToNotifications() {
        setActiveButton(notificationsBtn);
        loadView("/backoffice/notifications/notif-content.fxml");
    }

    @FXML
    public void logout() {
        Object session = getSessionManager();
        invokeNoArg(session, "logout");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        if (contentArea == null) {
            System.err.println("SidebarController: contentArea is null - call setContentArea() before navigating.");
            return;
        }

        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("[ERROR] FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            Object controller = loader.getController();

            injectIfPresent(controller, "setSidebarController", SidebarController.class, this);
            injectIfPresent(controller, "setContentArea", StackPane.class, contentArea);

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("[ERROR] Exception loading view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setTachesBadge(int count) {
        tachesBadge.setText(String.valueOf(count));
        tachesBadge.setVisible(count > 0);
    }

    public void setNotifBadge(int count) {
        notifBadge.setText(String.valueOf(count));
        notifBadge.setVisible(count > 0);
    }

    private Object getSessionManager() {
        try {
            Class<?> sessionManagerClass = Class.forName("piJava.utils.SessionManager");
            Method getInstance = sessionManagerClass.getMethod("getInstance");
            return getInstance.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Object invokeNoArg(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private void injectIfPresent(Object target, String methodName, Class<?> paramType, Object arg) {
        if (target == null) {
            return;
        }
        try {
            Method method = target.getClass().getMethod(methodName, paramType);
            method.invoke(target, arg);
        } catch (Exception ignored) {
        }
    }

    private boolean booleanValue(Object value) {
        return value instanceof Boolean && (Boolean) value;
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private static String buildInitials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isBlank()) ? prenom.substring(0, 1).toUpperCase() : "";
        String n = (nom != null && !nom.isBlank()) ? nom.substring(0, 1).toUpperCase() : "";
        return p + n;
    }

    private static String buildRoleDisplay(String roles) {
        if (roles == null || roles.isBlank()) return "Utilisateur";
        if (roles.contains("ROLE_ADMIN")) return "Administrateur";
        if (roles.contains("ROLE_ENSEIGNANT")) return "Enseignant";
        if (roles.contains("ROLE_ETUDIANT")) return "Etudiant";
        return "Utilisateur";
    }
}
