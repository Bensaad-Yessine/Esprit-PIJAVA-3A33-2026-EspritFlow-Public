package piJava.Controllers.frontoffice.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import piJava.entities.user;
import piJava.entities.Classe;
import piJava.services.UserServices;
import piJava.services.ClasseService;
import piJava.utils.SessionManager;
public class profileController implements Initializable {

    // ── Header / Avatar ──────────────────────────────────────────
    @FXML private Circle avatarCircle;
    @FXML private Label   avatarInitials;
    @FXML private Label   lblFullName;
    @FXML private Label   lblRole;
    @FXML private Label   lblVerified;

    // ── Ban badge ────────────────────────────────────────────────
    @FXML private HBox  banBadge;
    @FXML private Label lblBanReason;

    // ── Left sidebar stats ───────────────────────────────────────
    @FXML private Label lblJoinYear;
    @FXML private Label lblClasseStat;
    @FXML private Label lblEmailSmall;
    @FXML private Label lblTelSmall;
    @FXML private Label lblDobSmall;
    @FXML private Label lblSexeSmall;

    // ── Edit form ────────────────────────────────────────────────
    @FXML private TextField  tfPrenom;
    @FXML private TextField  tfNom;
    @FXML private TextField  tfEmail;
    @FXML private TextField  tfTel;
    @FXML private DatePicker dpDateNaissance;
    @FXML private ComboBox<String> cbSexe;
    @FXML private TextField  tfClasse;
    @FXML private Label      lblFormMsg;

    // ── Password section ─────────────────────────────────────────
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label         lblPasswordMsg;

    // ── Buttons ──────────────────────────────────────────────────
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    @FXML private Button btnChangePassword;

    // ── State ────────────────────────────────────────────────────
    private user currentUser;
    private final UserServices userServices = new UserServices();

    // ════════════════════════════════════════════════════════════
    //  initialize
    // ════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate sexe combo
        cbSexe.getItems().addAll("Homme", "Femme", "Autre");

        // Hover effects for buttons
        addHoverEffect(btnSave, "#C62828", "#EF5350");
        addHoverEffect(btnReset, "transparent", "#FFEBEE");

        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            this.currentUser = session.getCurrentUser();
            populateView(this.currentUser);
        } else {
            System.out.println("No user logged in!");
        }
    }
    // ════════════════════════════════════════════════════════════
    //  Public: load a user into the view
    // ════════════════════════════════════════════════════════════
    public void setUser(user u) {
        this.currentUser = u;
        populateView(u);
    }

    // ════════════════════════════════════════════════════════════
    //  Populate all UI elements from the user object
    // ════════════════════════════════════════════════════════════
    private void populateView(user u) {
        String prenom = nvl(u.getPrenom());
        String nom    = nvl(u.getNom());

        // ── Avatar initials ──────────────────────────────────
        String initials = initials(prenom, nom);
        avatarInitials.setText(initials);
        showAvatar(u.getProfile_pic());

        // ── Header ───────────────────────────────────────────
        lblFullName.setText(prenom + " " + nom);
        lblRole.setText(formatRole(u.getRoles()));
        lblVerified.setText(u.getIs_verified() == 1 ? "✔ Vérifié" : "✗ Non vérifié");
        lblVerified.setStyle(u.getIs_verified() == 1
                ? "-fx-background-color: rgba(76,175,80,0.3); -fx-background-radius: 20; -fx-text-fill: #A5D6A7; -fx-font-size: 11px; -fx-padding: 3 10;"
                : "-fx-background-color: rgba(255,152,0,0.3); -fx-background-radius: 20; -fx-text-fill: #FFCC80; -fx-font-size: 11px; -fx-padding: 3 10;");

        // ── Ban badge ─────────────────────────────────────────
        if (u.getIs_banned() == 1) {
            banBadge.setMaxHeight(Double.MAX_VALUE);
            banBadge.setOpacity(1);
            banBadge.setStyle("-fx-background-color: #B71C1C; -fx-padding: 8;");
            lblBanReason.setText(nvl(u.getBan_reason(), "Raison non spécifiée"));
        } else {
            banBadge.setMaxHeight(0);
            banBadge.setOpacity(0);
        }

        // ── Sidebar stats ─────────────────────────────────────
        String createdAt = nvl(u.getCreated_at(), "");
        lblJoinYear.setText(createdAt.length() >= 4 ? createdAt.substring(0, 4) : "—");

        String className = "—";
        if (u.getClasse_id() != null) {
            ClasseService classeService = new ClasseService();
            try {
                Classe c = classeService.getById(u.getClasse_id());
                if (c != null && c.getNom() != null) {
                    className = c.getNom();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        lblClasseStat.setText(className);

        lblEmailSmall.setText(nvl(u.getEmail(), "—"));
        lblTelSmall.setText(nvl(u.getNum_tel(), "—"));
        lblDobSmall.setText(nvl(u.getDate_de_naissance(), "—"));
        lblSexeSmall.setText(nvl(u.getSexe(), "—"));

        // ── Edit form pre-fill ────────────────────────────────
        tfPrenom.setText(prenom);
        tfNom.setText(nom);
        tfEmail.setText(nvl(u.getEmail()));
        tfTel.setText(nvl(u.getNum_tel()));
        tfClasse.setText(className);
        cbSexe.setValue(nvl(u.getSexe()));

        if (u.getDate_de_naissance() != null && !u.getDate_de_naissance().isEmpty()) {
            try {
                dpDateNaissance.setValue(LocalDate.parse(u.getDate_de_naissance()));
            } catch (Exception ignored) {}
        }

        lblFormMsg.setText("");
        lblPasswordMsg.setText("");
    }

    // ════════════════════════════════════════════════════════════
    //  FXML Handler: Save profile edits
    // ════════════════════════════════════════════════════════════
    @FXML
    private void handleSave() {
        if (currentUser == null) return;

        // ── Validation ──────────────────────────────────────
        String prenom = tfPrenom.getText().trim();
        String nom    = tfNom.getText().trim();
        String email  = tfEmail.getText().trim();
        String tel    = tfTel.getText().trim();
        String sexe   = cbSexe.getValue();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            showMsg(lblFormMsg, "❌ Prénom, nom et email sont requis.", false);
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showMsg(lblFormMsg, "❌ Adresse e-mail invalide.", false);
            return;
        }

        // ── Build updated user ───────────────────────────────
        currentUser.setPrenom(prenom);
        currentUser.setNom(nom);
        currentUser.setEmail(email);
        currentUser.setNum_tel(tel.isEmpty() ? null : tel);
        currentUser.setSexe(sexe);

        if (dpDateNaissance.getValue() != null) {
            currentUser.setDate_de_naissance(dpDateNaissance.getValue().toString());
        }

        // ── Persist ──────────────────────────────────────────
        userServices.edit(currentUser);
        showMsg(lblFormMsg, "✅ Profil mis à jour avec succès !", true);

        // Refresh header / sidebar from updated object
        refreshDisplayParts();
    }

    // ════════════════════════════════════════════════════════════
    //  FXML Handler: Reset form to original values
    // ════════════════════════════════════════════════════════════
    @FXML
    private void handleReset() {
        if (currentUser != null) {
            populateView(currentUser);
        }
        showMsg(lblFormMsg, "", true);
    }

    // ════════════════════════════════════════════════════════════
    //  FXML Handler: Edit Avatar
    // ════════════════════════════════════════════════════════════
    @FXML
    private void handleEditAvatar() {
        if (currentUser == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(avatarCircle.getScene().getWindow());
        if (file != null) {
            String path = file.toURI().toString();
            currentUser.setProfile_pic(path);
            showAvatar(path);
            userServices.edit(currentUser); // Save automatically or user can click Enregistrer
            showMsg(lblFormMsg, "✅ Avatar mis à jour avec succès !", true);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  FXML Handler: Change password
    // ════════════════════════════════════════════════════════════
    @FXML
    private void handleChangePassword() {
        if (currentUser == null) return;

        String newPwd     = pfNewPassword.getText();
        String confirmPwd = pfConfirmPassword.getText();

        if (newPwd.isEmpty()) {
            showMsg(lblPasswordMsg, "❌ Entrez un nouveau mot de passe.", false);
            return;
        }
        if (newPwd.length() < 6) {
            showMsg(lblPasswordMsg, "❌ Minimum 6 caractères.", false);
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showMsg(lblPasswordMsg, "❌ Les mots de passe ne correspondent pas.", false);
            return;
        }

        currentUser.setPassword(newPwd);
        userServices.edit(currentUser); // edit() hashes & saves if password non-empty
        currentUser.setPassword("");    // clear from memory
        pfNewPassword.clear();
        pfConfirmPassword.clear();
        showMsg(lblPasswordMsg, "✅ Mot de passe modifié avec succès !", true);
    }

    // ════════════════════════════════════════════════════════════
    //  Private helpers
    // ════════════════════════════════════════════════════════════
    private void refreshDisplayParts() {
        String prenom = nvl(currentUser.getPrenom());
        String nom    = nvl(currentUser.getNom());
        lblFullName.setText(prenom + " " + nom);
        avatarInitials.setText(initials(prenom, nom));
        lblEmailSmall.setText(nvl(currentUser.getEmail(), "—"));
        lblTelSmall.setText(nvl(currentUser.getNum_tel(), "—"));
        lblDobSmall.setText(nvl(currentUser.getDate_de_naissance(), "—"));
        lblSexeSmall.setText(nvl(currentUser.getSexe(), "—"));

        String className = "—";
        if (currentUser.getClasse_id() != null) {
            ClasseService classeService = new ClasseService();
            try {
                Classe c = classeService.getById(currentUser.getClasse_id());
                if (c != null && c.getNom() != null) {
                    className = c.getNom();
                }
            } catch (Exception e) {}
        }
        lblClasseStat.setText(className);
    }

    private void showAvatar(String url) {
        if (url == null || url.isEmpty()) {
            avatarCircle.setFill(Color.WHITE);
            avatarInitials.setVisible(true);
        } else {
            try {
                Image img = new Image(url, true);
                avatarCircle.setFill(new ImagePattern(img));
                avatarInitials.setVisible(false);
            } catch (Exception e) {
                avatarCircle.setFill(Color.WHITE);
                avatarInitials.setVisible(true);
            }
        }
    }

    private static String initials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isEmpty()) ? prenom.substring(0, 1).toUpperCase() : "?";
        String n = (nom    != null && !nom.isEmpty())    ? nom.substring(0, 1).toUpperCase()    : "?";
        return p + n;
    }

    private static String formatRole(String roles) {
        if (roles == null) return "Utilisateur";
        if (roles.contains("ROLE_ADMIN"))    return "👑 Administrateur";
        if (roles.contains("ROLE_TEACHER"))  return "🎓 Enseignant";
        return "🎒 Étudiant";
    }

    private static String nvl(String s)            { return s != null ? s : ""; }
    private static String nvl(String s, String def) { return (s != null && !s.isEmpty()) ? s : def; }

    private void showMsg(Label lbl, String msg, boolean success) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (success ? "#4CAF50" : "#F44336") + ";");
    }

    private void addHoverEffect(Button btn, String normal, String hover) {
        String base = btn.getStyle();
        btn.setOnMouseEntered(e -> btn.setStyle(base + "; -fx-background-color: " + hover + ";"));
        btn.setOnMouseExited (e -> btn.setStyle(base + "; -fx-background-color: " + normal + ";"));
    }

}
