package piJava.Controllers.frontoffice;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.util.Duration;
import piJava.entities.user;
import piJava.entities.Classe;
import piJava.services.ClasseService;
import piJava.services.StreakService;
import piJava.services.UserServices;
import piJava.utils.SessionManager;

public class FrontDashboardController implements Initializable {

    // ── Stats ──────────────────────────────────────────────────
    @FXML private Label totalStudentsLbl;
    @FXML private Label activeClassesLbl;
    @FXML private Label teachersLbl;
    @FXML private Label bannedUsersLbl;

    // ── Chart ──────────────────────────────────────────────────
    @FXML private BarChart<String, Number> enrollmentChart;

    // ── Recent students table ──────────────────────────────────
    @FXML private TableView<user>          studentsTable;
    @FXML private TableColumn<user,String> nameCol;
    @FXML private TableColumn<user,String> gradeCol;
    @FXML private TableColumn<user,String> courseCol;
    @FXML private TableColumn<user,String> attendanceCol;
    @FXML private TableColumn<user,String> statusCol;

    // ── Buttons ────────────────────────────────────────────────
    @FXML private Button newStudentBtn;
    @FXML private Button viewAllBtn;

    // ── Leaderboard container ──────────────────────────────────
    @FXML private VBox leaderboardContainer;

    // ── Services ───────────────────────────────────────────────
    private final UserServices  userServices  = new UserServices();
    private final ClasseService classeService = new ClasseService();
    private final StreakService  streakService = new StreakService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupHoverEffects();
        loadRealData();
    }

    // ── Data loading ───────────────────────────────────────────

    private void loadRealData() {
        try {
            List<user> users = userServices.show();

            // Stat cards
            long etudiants = users.stream()
                    .filter(u -> u.getRoles() != null
                            && u.getRoles().contains("ROLE_USER")
                            && !u.getRoles().contains("ROLE_ADMIN"))
                    .count();
            long profs  = users.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PROF"))
                    .count();
            long classes = classeService.show().size();

            if (totalStudentsLbl != null) totalStudentsLbl.setText(String.valueOf(etudiants));
            if (teachersLbl      != null) teachersLbl.setText(String.valueOf(profs));
            if (activeClassesLbl != null) activeClassesLbl.setText(String.valueOf(classes));

            setupTable(users);
            setupEnrollmentChart(users);
            buildLeaderboard();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Students table ─────────────────────────────────────────

    private void setupTable(List<user> users) {
        if (studentsTable == null) return;

        nameCol.setCellValueFactory(d -> new SimpleStringProperty(
                "#" + d.getValue().getId() + " - "
                + d.getValue().getPrenom() + " " + d.getValue().getNom()));

        gradeCol.setCellValueFactory(d -> {
            String r = d.getValue().getRoles();
            if (r == null) return new SimpleStringProperty("Étudiant");
            if (r.contains("ROLE_ADMIN")) return new SimpleStringProperty("Admin");
            if (r.contains("ROLE_PROF"))  return new SimpleStringProperty("Professeur");
            return new SimpleStringProperty("Étudiant");
        });

        courseCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        attendanceCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNum_tel()));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getIs_banned()   == 1 ? "Banni 🚫"    :
                d.getValue().getIs_verified() == 1 ? "Vérifié ✅"  : "Non vérifié ❌"));

        List<user> recent = users.stream()
                .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                .limit(10)
                .collect(Collectors.toList());
        studentsTable.setItems(FXCollections.observableArrayList(recent));
    }

    // ── Enrollment chart ───────────────────────────────────────

    private void setupEnrollmentChart(List<user> users) {
        if (enrollmentChart == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions Récentes");

        Map<String, Long> byMonth = users.stream()
                .filter(u -> u.getCreated_at() != null && u.getCreated_at().length() >= 7)
                .collect(Collectors.groupingBy(
                        u -> u.getCreated_at().substring(0, 7),
                        Collectors.counting()));

        byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData()
                        .add(new XYChart.Data<>(e.getKey(), e.getValue())));

        enrollmentChart.getData().clear();
        enrollmentChart.getData().add(series);
        enrollmentChart.setAnimated(true);
        enrollmentChart.setLegendVisible(false);
    }

    // ── Streak Leaderboard ─────────────────────────────────────

    private void buildLeaderboard() {
        if (leaderboardContainer == null) return;
        leaderboardContainer.getChildren().clear();

        List<user> top = streakService.getTopStreakUsers(10);

        if (top.isEmpty()) {
            Label empty = new Label("🔥 Aucun utilisateur avec un streak actif pour l'instant.");
            empty.getStyleClass().add("ldr-empty");
            leaderboardContainer.getChildren().add(empty);
            return;
        }

        // Current logged-in user ID for "You" badge
        int currentUserId = -1;
        try {
            user me = SessionManager.getInstance().getCurrentUser();
            if (me != null) currentUserId = me.getId();
        } catch (Exception ignored) {}

        for (int i = 0; i < top.size(); i++) {
            user u = top.get(i);
            int rank = i + 1;
            HBox row = buildLeaderboardRow(u, rank, currentUserId);

            // Slide-in animation
            row.setTranslateX(-30);
            row.setOpacity(0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(350), row);
            tt.setDelay(Duration.millis(i * 60L));
            tt.setToX(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition ft = new FadeTransition(Duration.millis(350), row);
            ft.setDelay(Duration.millis(i * 60L));
            ft.setFromValue(0); ft.setToValue(1);
            new ParallelTransition(tt, ft).play();

            leaderboardContainer.getChildren().add(row);
        }
    }

    private HBox buildLeaderboardRow(user u, int rank, int currentUserId) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        // Assign row style
        String rowStyle = switch (rank) {
            case 1 -> "row-gold";
            case 2 -> "row-silver";
            case 3 -> "row-bronze";
            default -> "leaderboard-row";
        };
        row.getStyleClass().add(rowStyle);

        // ── Rank badge ─────────────────────────────────────
        Label rankLbl = new Label();
        switch (rank) {
            case 1 -> { rankLbl.setText("🥇"); rankLbl.getStyleClass().add("rank-badge-gold"); }
            case 2 -> { rankLbl.setText("🥈"); rankLbl.getStyleClass().add("rank-badge-silver"); }
            case 3 -> { rankLbl.setText("🥉"); rankLbl.getStyleClass().add("rank-badge-bronze"); }
            default -> {
                rankLbl.setText(String.valueOf(rank));
                rankLbl.getStyleClass().add("rank-badge-other");
            }
        }
        rankLbl.setMinWidth(32);
        rankLbl.setAlignment(Pos.CENTER);

        // ── Avatar initials ─────────────────────────────────
        String[] avatarColors = avatarColors(u.getRoles());
        Label avatar = new Label(initials(u.getPrenom(), u.getNom()));
        avatar.getStyleClass().add("avatar-ldr");
        avatar.setStyle("-fx-background-color:" + avatarColors[0] + ";");
        avatar.setMinSize(36, 36); avatar.setMaxSize(36, 36);
        avatar.setAlignment(Pos.CENTER);

        // ── Name + subtitle ─────────────────────────────────
        String displayRole = roleLabel(u.getRoles());
        Label nameLbl = new Label(u.getPrenom() + " " + u.getNom());
        nameLbl.getStyleClass().add("ldr-name");
        Label subLbl  = new Label(displayRole + " · Last: " + formatDate(u.getLastStreakDate()));
        subLbl.getStyleClass().add("ldr-sub");

        VBox nameBox = new VBox(2, nameLbl, subLbl);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // ── "You" badge ─────────────────────────────────────
        HBox right = new HBox(8);
        right.setAlignment(Pos.CENTER_RIGHT);

        if (u.getId() == currentUserId) {
            Label youBadge = new Label("✦ Vous");
            youBadge.getStyleClass().add("streak-chip-you");
            right.getChildren().add(youBadge);
        }

        // ── Streak chip ─────────────────────────────────────
        Label streakChip = new Label("🔥 " + u.getCurrentStreak() + " jours");
        streakChip.getStyleClass().add("streak-chip");
        right.getChildren().add(streakChip);

        row.getChildren().addAll(rankLbl, avatar, nameBox, right);
        return row;
    }

    // ── Helpers ────────────────────────────────────────────────

    private String initials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isEmpty()) ? String.valueOf(prenom.charAt(0)) : "?";
        String n = (nom    != null && !nom.isEmpty())    ? String.valueOf(nom.charAt(0))    : "?";
        return (p + n).toUpperCase();
    }

    private String[] avatarColors(String roles) {
        if (roles == null) return new String[]{"#6366F1"};
        if (roles.contains("ROLE_ADMIN")) return new String[]{"#E63946"};
        if (roles.contains("ROLE_PROF"))  return new String[]{"#7C3AED"};
        return new String[]{"#059669"};
    }

    private String roleLabel(String roles) {
        if (roles == null) return "Étudiant";
        if (roles.contains("ROLE_ADMIN")) return "Admin";
        if (roles.contains("ROLE_PROF"))  return "Professeur";
        return "Étudiant";
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(iso,
                    java.time.format.DateTimeFormatter.ISO_DATE);
            return d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear();
        } catch (Exception e) {
            return iso;
        }
    }

    // ── Hover effects ──────────────────────────────────────────

    private void setupHoverEffects() {
        if (newStudentBtn != null) {
            newStudentBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
                    newStudentBtn.setStyle(
                            "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; " +
                            "-fx-font-weight: 600; -fx-background-radius: 8; -fx-cursor: hand; " +
                            "-fx-padding: 10 20 10 20;"));
            newStudentBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
                    newStudentBtn.setStyle(
                            "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 13px; " +
                            "-fx-font-weight: 600; -fx-background-radius: 8; -fx-cursor: hand; " +
                            "-fx-padding: 10 20 10 20;"));
        }

        if (viewAllBtn != null) {
            viewAllBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
                    viewAllBtn.setStyle(
                            "-fx-background-color: #2c1a1a; -fx-text-fill: #e74c3c; -fx-font-size: 13px; " +
                            "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 8 16 8 16; " +
                            "-fx-background-radius: 8;"));
            viewAllBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
                    viewAllBtn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 13px; " +
                            "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 8 16 8 16; " +
                            "-fx-background-radius: 8;"));
        }
    }
}
