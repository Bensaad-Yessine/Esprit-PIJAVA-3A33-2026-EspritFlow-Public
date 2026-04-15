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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ResourceBundle;

import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.user;
import piJava.entities.Classe;
import piJava.services.UserServices;
import piJava.services.ClasseService;
import piJava.utils.SessionManager;

public class profileController implements Initializable {

    @FXML private Circle avatarCircle;
    @FXML private Label   avatarInitials;
    @FXML private Label   lblFullName;
    @FXML private Label   lblRole;
    @FXML private Label   lblVerified;
    @FXML private HBox  banBadge;
    @FXML private Label lblBanReason;
    @FXML private Label lblJoinYear;
    @FXML private Label lblClasseStat;
    @FXML private Label lblEmailSmall;
    @FXML private Label lblTelSmall;
    @FXML private Label lblDobSmall;
    @FXML private Label lblSexeSmall;
    @FXML private TextField  tfPrenom;
    @FXML private TextField  tfNom;
    @FXML private TextField  tfEmail;
    @FXML private TextField  tfTel;
    @FXML private DatePicker dpDateNaissance;
    @FXML private ComboBox<String> cbSexe;
    @FXML private TextField  tfClasse;
    @FXML private Label      lblFormMsg;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label         lblPasswordMsg;
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    @FXML private Button btnChangePassword;

    private user currentUser;
    private final UserServices userServices = new UserServices();

    // ✅ Reference to sidebar for live refresh
    private FrontSidebarController sidebarController;

    private static final String UPLOAD_DIR =
            "C:\\Users\\MSI\\Documents\\my_project_dev\\public\\uploads\\profile_pics\\";

    /** Called by FrontSidebarController after loading this view */
    public void setSidebarController(FrontSidebarController sidebar) {
        this.sidebarController = sidebar;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbSexe.getItems().addAll("Homme", "Femme", "Autre");
        addHoverEffect(btnSave,  "#C62828", "#EF5350");
        addHoverEffect(btnReset, "transparent", "#FFEBEE");

        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            this.currentUser = session.getCurrentUser();
            populateView(this.currentUser);
        } else {
            System.out.println("No user logged in!");
        }
    }

    public void setUser(user u) {
        this.currentUser = u;
        populateView(u);
    }

    private void populateView(user u) {
        String prenom = nvl(u.getPrenom());
        String nom    = nvl(u.getNom());

        avatarInitials.setText(initials(prenom, nom));
        showAvatar(u.getProfile_pic());

        lblFullName.setText(prenom + " " + nom);
        lblRole.setText(formatRole(u.getRoles()));
        lblVerified.setText(u.getIs_verified() == 1 ? "✔ Vérifié" : "✗ Non vérifié");
        lblVerified.setStyle(u.getIs_verified() == 1
                ? "-fx-background-color: rgba(76,175,80,0.3); -fx-background-radius: 20; -fx-text-fill: #A5D6A7; -fx-font-size: 11px; -fx-padding: 3 10;"
                : "-fx-background-color: rgba(255,152,0,0.3); -fx-background-radius: 20; -fx-text-fill: #FFCC80; -fx-font-size: 11px; -fx-padding: 3 10;");

        if (u.getIs_banned() == 1) {
            banBadge.setMaxHeight(Double.MAX_VALUE);
            banBadge.setOpacity(1);
            banBadge.setStyle("-fx-background-color: #B71C1C; -fx-padding: 8;");
            lblBanReason.setText(nvl(u.getBan_reason(), "Raison non spécifiée"));
        } else {
            banBadge.setMaxHeight(0);
            banBadge.setOpacity(0);
        }

        String createdAt = nvl(u.getCreated_at(), "");
        lblJoinYear.setText(createdAt.length() >= 4 ? createdAt.substring(0, 4) : "—");

        String className = resolveClassName(u);
        lblClasseStat.setText(className);
        lblEmailSmall.setText(nvl(u.getEmail(), "—"));
        lblTelSmall.setText(nvl(u.getNum_tel(), "—"));
        lblDobSmall.setText(nvl(u.getDate_de_naissance(), "—"));
        lblSexeSmall.setText(nvl(u.getSexe(), "—"));

        tfPrenom.setText(prenom);
        tfNom.setText(nom);
        tfEmail.setText(nvl(u.getEmail()));
        tfTel.setText(nvl(u.getNum_tel()));
        tfClasse.setText(className);
        cbSexe.setValue(nvl(u.getSexe()));

        if (u.getDate_de_naissance() != null && !u.getDate_de_naissance().isEmpty()) {
            try { dpDateNaissance.setValue(LocalDate.parse(u.getDate_de_naissance())); }
            catch (Exception ignored) {}
        }

        lblFormMsg.setText("");
        lblPasswordMsg.setText("");
    }

    @FXML
    private void handleSave() {
        if (currentUser == null) return;

        String prenom = tfPrenom.getText().trim();
        String nom    = tfNom.getText().trim();
        String email  = tfEmail.getText().trim();
        String tel    = tfTel.getText().trim();
        String sexe   = cbSexe.getValue();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            showMsg(lblFormMsg, "❌ Prénom, nom et email sont requis.", false);
            return;
        }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s\\-]{2,}")) {
            showMsg(lblFormMsg, "❌ Le prénom doit contenir uniquement des lettres (min 2).", false);
            return;
        }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s\\-]{2,}")) {
            showMsg(lblFormMsg, "❌ Le nom doit contenir uniquement des lettres (min 2).", false);
            return;
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showMsg(lblFormMsg, "❌ Adresse e-mail invalide.", false);
            return;
        }

        currentUser.setPrenom(prenom);
        currentUser.setNom(nom);
        currentUser.setEmail(email);
        currentUser.setNum_tel(tel.isEmpty() ? null : tel);
        currentUser.setSexe(sexe);
        if (dpDateNaissance.getValue() != null)
            currentUser.setDate_de_naissance(dpDateNaissance.getValue().toString());

        userServices.edit(currentUser);
        showMsg(lblFormMsg, "✅ Profil mis à jour avec succès !", true);
        refreshDisplayParts();

        // ✅ Refresh sidebar live (name, email)
        if (sidebarController != null) sidebarController.refreshSessionData();
    }

    @FXML
    private void handleReset() {
        if (currentUser != null) populateView(currentUser);
        showMsg(lblFormMsg, "", true);
    }

    @FXML
    private void handleEditAvatar() {
        if (currentUser == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        File file = fileChooser.showOpenDialog(avatarCircle.getScene().getWindow());
        if (file != null) {
            String savedName = copyToUploads(file);
            if (savedName != null) {
                currentUser.setProfile_pic(savedName);
                userServices.edit(currentUser);
                showAvatar(savedName);  // ✅ repaint the big circle immediately

                // ✅ Repaint sidebar avatar immediately — no logout needed
                if (sidebarController != null) sidebarController.refreshSessionData();

                showMsg(lblFormMsg, "✅ Avatar mis à jour avec succès !", true);
            } else {
                showMsg(lblFormMsg, "❌ Impossible d'enregistrer l'image.", false);
            }
        }
    }

    private String copyToUploads(File source) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String ext = source.getName().contains(".")
                    ? source.getName().substring(source.getName().lastIndexOf('.')) : ".jpg";
            String filename = "profile_" + System.currentTimeMillis() + ext;
            Files.copy(source.toPath(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            System.err.println("Failed to copy profile pic: " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void handleChangePassword() {
        if (currentUser == null) return;

        String newPwd     = pfNewPassword.getText();
        String confirmPwd = pfConfirmPassword.getText();

        if (newPwd.isEmpty()) { showMsg(lblPasswordMsg, "❌ Entrez un nouveau mot de passe.", false); return; }
        if (newPwd.length() < 6) { showMsg(lblPasswordMsg, "❌ Minimum 6 caractères.", false); return; }
        if (!newPwd.equals(confirmPwd)) { showMsg(lblPasswordMsg, "❌ Les mots de passe ne correspondent pas.", false); return; }

        currentUser.setPassword(newPwd);
        userServices.edit(currentUser);
        currentUser.setPassword(""); // ✅ clear plain-text from memory immediately
        pfNewPassword.clear();
        pfConfirmPassword.clear();
        showMsg(lblPasswordMsg, "✅ Mot de passe modifié avec succès !", true);
    }

    private void refreshDisplayParts() {
        String prenom = nvl(currentUser.getPrenom());
        String nom    = nvl(currentUser.getNom());
        lblFullName.setText(prenom + " " + nom);
        avatarInitials.setText(initials(prenom, nom));
        lblEmailSmall.setText(nvl(currentUser.getEmail(), "—"));
        lblTelSmall.setText(nvl(currentUser.getNum_tel(), "—"));
        lblDobSmall.setText(nvl(currentUser.getDate_de_naissance(), "—"));
        lblSexeSmall.setText(nvl(currentUser.getSexe(), "—"));
        lblClasseStat.setText(resolveClassName(currentUser));
    }

    // ✅ KEY FIX: load synchronously (background=false) — eliminates the white-flash
    private void showAvatar(String picFilename) {
        if (picFilename == null || picFilename.isEmpty()) {
            avatarCircle.setFill(Color.web("#C62828"));
            avatarInitials.setVisible(true);
            return;
        }
        try {
            String url;
            if (picFilename.startsWith("http") || picFilename.startsWith("file:")) {
                url = picFilename;
            } else {
                File imgFile = new File(UPLOAD_DIR + picFilename);
                url = imgFile.exists()
                        ? imgFile.toURI().toString()
                        : "file:///" + UPLOAD_DIR.replace("\\", "/") + picFilename;
            }
            Image img = new Image(url, false); // ✅ false = synchronous, no white flash
            if (!img.isError()) {
                avatarCircle.setFill(new ImagePattern(img));
                avatarInitials.setVisible(false);
            } else {
                avatarCircle.setFill(Color.web("#C62828"));
                avatarInitials.setVisible(true);
            }
        } catch (Exception e) {
            avatarCircle.setFill(Color.web("#C62828"));
            avatarInitials.setVisible(true);
        }
    }

    private String resolveClassName(user u) {
        if (u.getClasse_id() == null) return "—";
        try {
            Classe c = new ClasseService().getById(u.getClasse_id());
            return (c != null && c.getNom() != null) ? c.getNom() : "—";
        } catch (Exception e) { return "—"; }
    }

    private static String initials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isEmpty()) ? prenom.substring(0, 1).toUpperCase() : "?";
        String n = (nom    != null && !nom.isEmpty())    ? nom.substring(0, 1).toUpperCase()    : "?";
        return p + n;
    }

    private static String formatRole(String roles) {
        if (roles == null)                  return "Utilisateur";
        if (roles.contains("ROLE_ADMIN"))   return "👑 Administrateur";
        if (roles.contains("ROLE_TEACHER")) return "🎓 Enseignant";
        return "🎒 Étudiant";
    }

    private static String nvl(String s)             { return s != null ? s : ""; }
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