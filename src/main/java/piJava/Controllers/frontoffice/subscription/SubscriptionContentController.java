package piJava.Controllers.frontoffice.subscription;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.SubscriptionPack;
import piJava.entities.user;
import piJava.services.StripeCheckoutService;
import piJava.services.SubscriptionPackService;
import piJava.utils.SessionManager;
import piJava.utils.SubscriptionCurrency;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SubscriptionContentController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label summaryLabel;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    @FXML private FlowPane packsContainer;

    private final SubscriptionPackService subscriptionPackService = new SubscriptionPackService();
    private final StripeCheckoutService stripeCheckoutService = new StripeCheckoutService();

    private FrontSidebarController sidebarController;
    private user currentUser;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        updateHeader();
        loadPacks();
    }

    @FXML
    private void handleReload() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        updateHeader();
        loadPacks();
    }

    private void updateHeader() {
        if (currentUser == null) {
            titleLabel.setText("Abonnements et paiement Stripe");
            subtitleLabel.setText("Connectez-vous pour acheter un pack.");
            summaryLabel.setText("Aucun utilisateur connecté");
            statusLabel.setText("Session indisponible");
            return;
        }

        titleLabel.setText("Abonnements et paiement Stripe");
        subtitleLabel.setText("Choisissez un pack et payez en toute sécurité via Stripe Checkout.");
        summaryLabel.setText("Connecté en tant que " + safe(currentUser.getPrenom()) + " " + safe(currentUser.getNom()) + " · " + safe(currentUser.getEmail()));
        statusLabel.setText("Prêt à payer");
    }

    private void loadPacks() {
        packsContainer.getChildren().clear();
        messageLabel.setText("");

        List<SubscriptionPack> packs;
        try {
            packs = subscriptionPackService.getAll();
        } catch (Exception e) {
            showMessage("Erreur lors du chargement des packs: " + e.getMessage(), true);
            packs = new ArrayList<>();
        }

        if (packs.isEmpty()) {
            packsContainer.getChildren().add(buildEmptyState("Aucun pack d'abonnement trouvé dans la table subscription_pack."));
            return;
        }

        ObservableList<SubscriptionPack> items = FXCollections.observableArrayList(packs);
        for (SubscriptionPack pack : items) {
            packsContainer.getChildren().add(buildPackCard(pack));
        }
    }

    private VBox buildPackCard(SubscriptionPack pack) {
        String accent = normalizeColor(pack.getColor(), "#C62828");

        VBox card = new VBox(12);
        card.getStyleClass().add("subscription-card");
        card.setPrefWidth(330);
        card.setMinWidth(300);
        card.setMaxWidth(360);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-border-color: " + accent + ";");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(resolveIcon(pack.getIcon()));
        icon.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: " + accent + "; -fx-background-radius: 999; -fx-padding: 10 12;");

        VBox titleBox = new VBox(2);
        Label name = new Label(pack.getName() != null ? pack.getName() : "Pack");
        name.getStyleClass().add("subscription-card-title");
        Label meta = new Label("Durée : " + pack.getDurationDays() + " jour(s)");
        meta.getStyleClass().add("subscription-card-meta");
        titleBox.getChildren().addAll(name, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox badges = new HBox(6);
        badges.setAlignment(Pos.CENTER_RIGHT);
        if (pack.isPopular()) {
            badges.getChildren().add(badge("Populaire", "#F59E0B"));
        }
        badges.getChildren().add(badge(pack.isActive() ? "Actif" : "Inactif", pack.isActive() ? "#10B981" : "#6B7280"));

        topRow.getChildren().addAll(icon, titleBox, spacer, badges);

        Label price = new Label(formatPrice(pack.getPrice(), pack.getCurrency()));
        price.getStyleClass().add("subscription-card-price");

        Label description = new Label(safe(pack.getDescription(), "Aucune description disponible."));
        description.setWrapText(true);
        description.getStyleClass().add("subscription-card-description");

        VBox featureBox = new VBox(6);
        featureBox.getChildren().add(featureHeader());
        List<String> features = splitFeatures(pack.getFeatures());
        if (features.isEmpty()) {
            featureBox.getChildren().add(featureRow("•", "Accès aux fonctionnalités détaillées du pack"));
        } else {
            for (String feature : features) {
                featureBox.getChildren().add(featureRow("•", feature));
            }
        }

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        Button buyButton = new Button(pack.isActive() ? "Payer avec Stripe" : "Pack désactivé");
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setDisable(!pack.isActive());
        buyButton.setStyle("-fx-background-color: linear-gradient(to right, #E63946, #C1121F); -fx-text-fill: white; "
                + "-fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 10 16; -fx-cursor: hand;");
        buyButton.setOnAction(e -> handleCheckout(pack));
        HBox.setHgrow(buyButton, Priority.ALWAYS);

        Label currencyHint = new Label(SubscriptionCurrency.normalize(pack.getCurrency()));
        currencyHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #C1121F; -fx-background-color: #FEE2E4; -fx-background-radius: 999; -fx-padding: 6 10; -fx-font-weight: 700;");

        actionRow.getChildren().addAll(buyButton, currencyHint);

        card.getChildren().addAll(topRow, price, description, featureBox, actionRow);
        return card;
    }

    private VBox buildEmptyState(String message) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(24));
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(520);
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 18; -fx-border-color: #e9ecef; -fx-border-radius: 18; -fx-border-width: 1;");

        Label title = new Label("Aucun pack disponible");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #212529;");
        Label body = new Label(message);
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-alignment: center;");

        box.getChildren().addAll(title, body);
        return box;
    }

    private void handleCheckout(SubscriptionPack pack) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showMessage("Veuillez vous connecter avant de lancer un paiement.", true);
            return;
        }

        showMessage("Ouverture de Stripe pour le pack : " + pack.getName(), false);
        stripeCheckoutService.startCheckout(currentUser, pack, new StripeCheckoutService.PaymentCallback() {
            @Override
            public void onSuccess(String message) {
                showMessage(message, false);
                showInfoDialog("Paiement Stripe", message);
                refreshAfterPayment();
            }

            @Override
            public void onCancel(String message) {
                showMessage(message, true);
                showInfoDialog("Paiement annulé", message);
            }

            @Override
            public void onError(String message) {
                showMessage(message, true);
                showInfoDialog("Erreur Stripe", message);
            }
        });
    }

    private void refreshAfterPayment() {
        Platform.runLater(() -> {
            loadPacks();
            if (sidebarController != null) {
                sidebarController.refreshSessionData();
            }
        });
    }

    private HBox featureHeader() {
        HBox header = new HBox();
        Label label = new Label("Fonctionnalités incluses");
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #6c757d;");
        header.getChildren().add(label);
        return header;
    }

    private HBox featureRow(String bullet, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label bulletLabel = new Label(bullet);
        bulletLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #E63946;");
        Label featureLabel = new Label(text);
        featureLabel.setWrapText(true);
        featureLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");
        row.getChildren().addAll(bulletLabel, featureLabel);
        return row;
    }

    private Label badge(String text, String color) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 700; -fx-background-radius: 999; -fx-padding: 5 10;");
        return badge;
    }

    private List<String> splitFeatures(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String[] parts = raw.split("[\n,;|]+");
        List<String> features = new ArrayList<>();
        for (String part : parts) {
            String cleaned = part.trim();
            if (!cleaned.isEmpty()) {
                features.add(cleaned);
            }
        }
        return features;
    }

    private String formatPrice(BigDecimal price, String currency) {
        BigDecimal safePrice = price != null ? price : BigDecimal.ZERO;
        String safeCurrency = SubscriptionCurrency.normalize(currency);
        return safePrice.stripTrailingZeros().toPlainString() + " " + SubscriptionCurrency.symbol(safeCurrency) + " (" + safeCurrency + ")";
    }

    private String resolveIcon(String icon) {
        String value = safe(icon, "★");
        return value.isBlank() ? "★" : value;
    }

    private String normalizeColor(String color, String fallback) {
        String value = safe(color, fallback);
        if (!value.startsWith("#") || value.length() < 4) {
            return fallback;
        }
        return value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        String normalized = safe(value);
        return normalized.isEmpty() ? fallback : normalized;
    }

    private void showMessage(String message, boolean error) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + (error ? "#FF8A80" : "#A5D6A7") + ";");
        });
    }

    private void showInfoDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}

