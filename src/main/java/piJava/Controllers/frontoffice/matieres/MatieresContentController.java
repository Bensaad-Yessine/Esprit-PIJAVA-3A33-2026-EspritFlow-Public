package piJava.Controllers.frontoffice.matieres;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.Classe;
import piJava.entities.Matiere;
import piJava.entities.MatiereGlobalStats;
import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.services.MatiereService;
import piJava.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MatieresContentController implements Initializable {

	@FXML private Label lblClassName;
	@FXML private Label lblClassLevel;
	@FXML private Label lblClassYear;
	@FXML private Label lblMatieresCount;
	@FXML private Label lblGlobalCoeffMoyen;
	@FXML private Label lblGlobalChargeMoyenne;
	@FXML private Label lblGlobalComplexiteMoyenne;
	@FXML private Label lblGlobalMatieresSansClasse;
	@FXML private Label lblStatusMessage;
	@FXML private TextField txtSearch;

	@FXML private TableView<Matiere> matiereTable;
	@FXML private TableColumn<Matiere, String> nomCol;
	@FXML private TableColumn<Matiere, String> coeffCol;
	@FXML private TableColumn<Matiere, String> heuresCol;
	@FXML private TableColumn<Matiere, String> complexiteCol;
	@FXML private TableColumn<Matiere, String> descCol;

	private final ClasseService classeService = new ClasseService();
	private final MatiereService matiereService = new MatiereService();
	private final ObservableList<Matiere> matieres = FXCollections.observableArrayList();
	private final FilteredList<Matiere> filteredMatieres = new FilteredList<>(matieres, m -> true);

	public void setSidebarController(FrontSidebarController sidebarController) {
		// Navigation future si besoin.
	}

	public void setContentArea(StackPane contentArea) {
		// Navigation future si besoin.
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		setupColumns();
		setupSearch();
		SortedList<Matiere> sortedMatieres = new SortedList<>(filteredMatieres);
		sortedMatieres.comparatorProperty().bind(matiereTable.comparatorProperty());
		matiereTable.setItems(sortedMatieres);
		matiereTable.setPlaceholder(new Label("Aucune matière à afficher."));
		loadMatieresForConnectedUser();
	}

	private void setupSearch() {
		if (txtSearch == null) {
			return;
		}
		txtSearch.textProperty().addListener((obs, oldValue, newValue) -> applySearchFilter(newValue));
	}

	private void setupColumns() {
		nomCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getNom(), "—")));
		coeffCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f", d.getValue().getCoefficient())));
		heuresCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChargehoraire() + " h"));
		complexiteCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getScorecomplexite() + "/10"));
		descCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getDescription(), "")));

		nomCol.setCellFactory(col -> textCell("-fx-text-fill: #1f2937; -fx-font-weight: 700; -fx-font-size: 13px;"));
		coeffCol.setCellFactory(col -> badgeCell("#ede9fe", "#c4b5fd", "#6d28d9"));
		heuresCol.setCellFactory(col -> badgeCell("#dbeafe", "#93c5fd", "#1d4ed8"));
		complexiteCol.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.isBlank()) {
					setGraphic(null);
					setText(null);
					return;
				}

				int score;
				try {
					score = Integer.parseInt(item.replace("/10", ""));
				} catch (NumberFormatException e) {
					score = 0;
				}

				Label badge = new Label(item);
				badge.setStyle(complexiteStyle(score));
				setGraphic(badge);
				setText(null);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}
		});
		descCol.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.isBlank()) {
					setGraphic(null);
					setText(null);
					return;
				}
				String truncated = item.length() > 70 ? item.substring(0, 70) + "…" : item;
				Label lbl = new Label(truncated);
				lbl.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
				Tooltip.install(lbl, new Tooltip(item));
				setGraphic(lbl);
				setText(null);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}
		});
	}

	private void loadMatieresForConnectedUser() {
		SessionManager session = SessionManager.getInstance();
		user currentUser = session.getCurrentUser();

		if (currentUser == null) {
			showEmptyState("Aucun utilisateur connecté.");
			return;
		}

		Integer classId = currentUser.getClasse_id();
		if (classId == null) {
			showEmptyState("Votre compte n’est rattaché à aucune classe.");
			return;
		}

		try {
			MatiereGlobalStats globalStats = matiereService.getStatistiquesGlobales();
			lblGlobalCoeffMoyen.setText(String.format("%.2f", globalStats.getCoefficientMoyen()));
			lblGlobalChargeMoyenne.setText(String.format("%.1f h", globalStats.getChargeHoraireMoyenne()));
			lblGlobalComplexiteMoyenne.setText(String.format("%.1f/10", globalStats.getComplexiteMoyenne()));
			lblGlobalMatieresSansClasse.setText(String.valueOf(globalStats.getMatieresSansClasse()));

			Classe classe = classeService.getById(classId);
			if (classe == null) {
				showEmptyState("Classe introuvable.");
				return;
			}

			lblClassName.setText(nvl(classe.getNom(), "—"));
			lblClassLevel.setText(nvl(classe.getNiveau(), "—"));
			lblClassYear.setText(nvl(classe.getAnneeUniversitaire(), "—"));

			matieres.setAll(matiereService.getMatieresByClasseId(classe.getId()));
			applySearchFilter(txtSearch != null ? txtSearch.getText() : "");
			if (matieres.isEmpty()) {
				lblStatusMessage.setText("Aucune matière liée à cette classe pour le moment.");
				lblStatusMessage.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fcd34d; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
			} else if (filteredMatieres.isEmpty()) {
				lblStatusMessage.setText("Aucun résultat pour votre recherche.");
				lblStatusMessage.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fcd34d; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
			} else {
				lblStatusMessage.setText("Matières chargées avec succès.");
				lblStatusMessage.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
			}
		} catch (SQLException e) {
			showEmptyState("Erreur lors du chargement des matières : " + e.getMessage());
		}
	}

	@FXML
	public void handleRefresh() {
		loadMatieresForConnectedUser();
	}

	private void showEmptyState(String message) {
		lblClassName.setText("—");
		lblClassLevel.setText("—");
		lblClassYear.setText("—");
		lblMatieresCount.setText("0 matière");
		lblGlobalCoeffMoyen.setText("—");
		lblGlobalChargeMoyenne.setText("—");
		lblGlobalComplexiteMoyenne.setText("—");
		lblGlobalMatieresSansClasse.setText("—");
		lblStatusMessage.setText(message);
		lblStatusMessage.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #fca5a5; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #b91c1c; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
		matieres.clear();
		if (txtSearch != null) {
			txtSearch.clear();
		}
	}

	private void applySearchFilter(String query) {
		String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		filteredMatieres.setPredicate(matiere -> {
			if (normalized.isEmpty()) {
				return true;
			}
			return containsIgnoreCase(matiere.getNom(), normalized)
					|| containsIgnoreCase(matiere.getDescription(), normalized)
					|| String.valueOf(matiere.getCoefficient()).contains(normalized)
					|| String.valueOf(matiere.getChargehoraire()).contains(normalized)
					|| String.valueOf(matiere.getScorecomplexite()).contains(normalized);
		});

		int total = matieres.size();
		int visible = filteredMatieres.size();
		if (normalized.isEmpty()) {
			lblMatieresCount.setText(total + " matière(s)");
			if (total > 0) {
				lblStatusMessage.setText("Matières chargées avec succès.");
				lblStatusMessage.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
			}
		} else {
			lblMatieresCount.setText(visible + " / " + total + " matière(s)");
			if (total > 0 && visible == 0) {
				lblStatusMessage.setText("Aucun résultat pour votre recherche.");
				lblStatusMessage.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fcd34d; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
			} else if (visible > 0) {
				lblStatusMessage.setText(visible + " matière(s) trouvée(s).");
				lblStatusMessage.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14 6 14;");
			}
		}
	}

	private boolean containsIgnoreCase(String source, String token) {
		return source != null && source.toLowerCase(Locale.ROOT).contains(token);
	}

	private TableCell<Matiere, String> badgeCell(String bg, String border, String fg) {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.isBlank()) {
					setGraphic(null);
					setText(null);
					return;
				}
				Label badge = new Label(item);
				badge.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; "
						+ "-fx-border-width: 1; -fx-border-radius: 999; -fx-background-radius: 999; "
						+ "-fx-text-fill: " + fg + "; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 4 10;");
				setGraphic(badge);
				setText(null);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}
		};
	}

	private TableCell<Matiere, String> textCell(String style) {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.isBlank()) {
					setGraphic(null);
					setText(null);
					return;
				}
				Label lbl = new Label(item);
				lbl.setStyle(style);
				setGraphic(lbl);
				setText(null);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}
		};
	}

	private String complexiteStyle(int score) {
		if (score <= 3) return "-fx-background-color: #dcfce7; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 999; -fx-background-radius: 999; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 4 10;";
		if (score <= 6) return "-fx-background-color: #fef3c7; -fx-border-color: #fcd34d; -fx-border-width: 1; -fx-border-radius: 999; -fx-background-radius: 999; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 4 10;";
		if (score <= 8) return "-fx-background-color: #ffedd5; -fx-border-color: #fdba74; -fx-border-width: 1; -fx-border-radius: 999; -fx-background-radius: 999; -fx-text-fill: #c2410c; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 4 10;";
		return "-fx-background-color: #fee2e2; -fx-border-color: #fca5a5; -fx-border-width: 1; -fx-border-radius: 999; -fx-background-radius: 999; -fx-text-fill: #b91c1c; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 4 10;";
	}

	private static String nvl(String value, String fallback) {
		return (value != null && !value.isBlank()) ? value : fallback;
	}
}


