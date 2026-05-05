package piJava.Controllers.frontoffice.matieres;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.api.ExportApi;
import piJava.api.WikipediaApi;
import piJava.entities.Classe;
import piJava.entities.Matiere;
import piJava.entities.MatiereGlobalStats;
import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.services.MatiereService;
import piJava.utils.SessionManager;

import java.io.File;
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
	@FXML private TableColumn<Matiere, Void> wikiCol;
	@FXML private TableColumn<Matiere, Void> quizCol;

	private final ClasseService classeService = new ClasseService();
	private final MatiereService matiereService = new MatiereService();
	private final ObservableList<Matiere> matieres = FXCollections.observableArrayList();
	private final FilteredList<Matiere> filteredMatieres = new FilteredList<>(matieres, m -> true);

	private StackPane contentArea;

	public void setSidebarController(FrontSidebarController sidebarController) {
		// Navigation future si besoin.
	}

	public void setContentArea(StackPane contentArea) {
		this.contentArea = contentArea;
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

		// Action lors de la sélection d'une ligne pour Wikipedia (comme dans le backoffice)
		matiereTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				// Optionnel: On peut garder le bouton Wiki OU charger automatiquement
				// Si on veut charger automatiquement comme dans le backoffice, on appellerait une méthode ici
			}
		});
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

		wikiCol.setCellFactory(col -> new TableCell<>() {
			private final Button btnWiki = new Button("ℹ Wiki");
			{
				btnWiki.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;");
				btnWiki.setOnAction(event -> {
					Matiere m = getTableView().getItems().get(getIndex());
					showWikipediaSummary(m.getNom());
				});
				btnWiki.setOnMouseEntered(e -> btnWiki.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;"));
				btnWiki.setOnMouseExited(e -> btnWiki.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;"));
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(btnWiki);
				}
			}
		});

		if (quizCol != null) {
			quizCol.setCellFactory(col -> new TableCell<>() {
				private final Button btnQuiz = new Button("Passer le quiz");
				{
					btnQuiz.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5;");
					btnQuiz.setOnAction(event -> {
						Matiere m = getTableView().getItems().get(getIndex());
						launchQuiz(m);
					});
				}

				@Override
				protected void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setGraphic(null);
					} else {
						setGraphic(btnQuiz);
					}
				}
			});
		}
	}

	private void launchQuiz(Matiere matiere) {
		if (contentArea == null) {
			System.err.println("contentArea is null in MatieresContentController.");
			return;
		}
		try {
			java.net.URL resource = getClass().getResource("/frontoffice/quiz/quiz-attempt.fxml");
			javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
			javafx.scene.layout.Region view = loader.load();
			
			piJava.Controllers.frontoffice.quiz.QuizAttemptController controller = loader.getController();
			controller.setContentArea(contentArea);
			controller.initData(matiere);
			
			contentArea.getChildren().setAll(view);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showWikipediaSummary(String subject) {
		// Utilisation de Platform.runLater pour s'assurer que l'UI reste fluide
		new Thread(() -> {
			String summary = WikipediaApi.getSummary(subject);
			javafx.application.Platform.runLater(() -> {
				renderWikiDialog(subject, summary);
			});
		}).start();
	}

	private void renderWikiDialog(String subject, String summary) {
		Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initStyle(StageStyle.UNDECORATED);

		// ── HEADER ──────────────────────────────────────────────────
		Label icon = new Label("📖");
		icon.setStyle("-fx-font-size: 30px;");

		Label titleLbl = new Label("Wikipédia · " + subject);
		titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");

		Label subLbl = new Label("Informations issues de Wikipédia");
		subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-family: 'Segoe UI';");

		VBox titleBox = new VBox(3, titleLbl, subLbl);
		titleBox.setAlignment(Pos.CENTER_LEFT);

		HBox header = new HBox(16, icon, titleBox);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(24, 28, 24, 28));
		header.setStyle(
			"-fx-background-color: linear-gradient(to right, #E63946, #C1121F);" +
			"-fx-background-radius: 20 20 0 0;"
		);

		// ── CONTENU ─────────────────────────────────────────────────
		Label contentLbl = new Label(summary);
		contentLbl.setWrapText(true);
		contentLbl.setStyle(
			"-fx-font-size: 14px;" +
			"-fx-text-fill: #374151;" +
			"-fx-font-family: 'Segoe UI';"
		);

		VBox contentCard = new VBox(contentLbl);
		contentCard.setPadding(new Insets(18));
		contentCard.setStyle(
			"-fx-background-color: #fff5f6;" +
			"-fx-background-radius: 14;" +
			"-fx-border-color: #fecdd3;" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 14;"
		);

		ScrollPane scrollPane = new ScrollPane(contentCard);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(320);
		scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

		VBox body = new VBox(scrollPane);
		body.setPadding(new Insets(20, 28, 20, 28));
		body.setStyle("-fx-background-color: white;");

		// ── FOOTER ──────────────────────────────────────────────────
		Button closeBtn = new Button("✕  Fermer");
		closeBtn.setStyle(
			"-fx-background-color: linear-gradient(to right, #E63946, #C1121F);" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 13px;" +
			"-fx-font-weight: 800;" +
			"-fx-font-family: 'Segoe UI';" +
			"-fx-background-radius: 12;" +
			"-fx-padding: 10 28 10 28;" +
			"-fx-cursor: hand;"
		);
		closeBtn.setOnAction(e -> dialog.close());

		Hyperlink readMoreLink = new Hyperlink("Lire l'article complet sur Wikipédia ↗");
		readMoreLink.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px; -fx-font-weight: 600;");
		readMoreLink.setOnAction(e -> {
			try {
				String url = "https://fr.wikipedia.org/wiki/" + java.net.URLEncoder.encode(subject.replace(" ", "_"), "UTF-8");
				new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		HBox footer = new HBox(15, readMoreLink, closeBtn);
		footer.setAlignment(Pos.CENTER_RIGHT);
		footer.setPadding(new Insets(14, 28, 20, 28));
		footer.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 0 0 20 20;" +
			"-fx-border-color: #fee2e2 transparent transparent transparent;" +
			"-fx-border-width: 1 0 0 0;"
		);

		// ── ROOT ────────────────────────────────────────────────────
		VBox root = new VBox(header, body, footer);
		root.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 20;" +
			"-fx-border-color: #fecdd3;" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 20;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0.12, 0, 8);"
		);

		javafx.scene.Scene scene = new javafx.scene.Scene(root, 620, 500);
		scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
		dialog.setScene(scene);
		dialog.show();
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

	@FXML
	public void handleExportExcel() {
		if (matieres.isEmpty()) {
			showAlert("Information", "Aucune matière à exporter.", Alert.AlertType.INFORMATION);
			return;
		}

		try {
			// Déterminer le chemin du Bureau (gère OneDrive)
			String userHome = System.getProperty("user.home");
			File desktop = new File(userHome, "Desktop");
			if (!desktop.exists()) {
				desktop = new File(userHome, "OneDrive" + File.separator + "Bureau");
			}
			if (!desktop.exists()) {
				desktop = new File(userHome, "OneDrive" + File.separator + "Desktop");
			}

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Enregistrer la liste des matières (Excel)");

			if (desktop.exists()) {
				fileChooser.setInitialDirectory(desktop);
			}

			fileChooser.setInitialFileName("Liste_Matieres.xlsx");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));

			File file = fileChooser.showSaveDialog(null);
			if (file != null) {
				System.out.println("Tentative d'export Excel vers : " + file.getAbsolutePath());
				ExportApi.exportMatieresToExcel(matieres, file.getAbsolutePath());
				showAlert("Succès", "La liste des matières a été exportée avec succès à l'emplacement :\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
			}
		} catch (Exception e) {
			showAlert("Erreur", "Erreur lors de l'export Excel: " + e.getMessage(), Alert.AlertType.ERROR);
			e.printStackTrace();
		}
	}

	private void showAlert(String title, String content, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
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

