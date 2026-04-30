package piJava.Controllers.frontoffice;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import piJava.services.AcademicChatService;
import piJava.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ChatController — Floating academic chatbot widget (EspritBot).
 *
 * Features:
 *  - GPT-4 via RapidAPI (multi-turn conversation)
 *  - Markdown code-block rendering (monospace)
 *  - Animated open / close (FAB)
 *  - Typing indicator with pulsing animation
 *  - Quick suggestion chips on welcome screen
 *  - Clear / reset conversation
 */
public class ChatController implements Initializable {

    // ── FXML injections ────────────────────────────────────────
    // NOTE: chatRoot is the FXML root node — cannot be injected with @FXML.
    @FXML private Button     fabBtn;
    @FXML private VBox       chatPanel;
    @FXML private ScrollPane messagesScroll;
    @FXML private VBox       messagesArea;
    @FXML private TextField  inputField;
    @FXML private Button     sendBtn;
    @FXML private Button     clearBtn;
    @FXML private HBox       suggestionsRow;

    // ── State ──────────────────────────────────────────────────
    private boolean panelOpen = false;
    private final AcademicChatService chatService = new AcademicChatService();

    // ── Quick suggestion prompts ───────────────────────────────
    private static final List<String> SUGGESTIONS = List.of(
            "📐 Explique la complexité algorithmique",
            "☕ Différence ArrayList vs LinkedList",
            "🗄️ JOIN SQL avec exemple",
            "🌐 Comment fonctionne HTTP/HTTPS ?",
            "📊 Qu'est-ce que la récursivité ?"
    );

    // ── Initializable ──────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Panel is hidden at startup
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);
        chatPanel.setOpacity(0);

        setupInputKeyHandler();
        showWelcomeMessage();
        buildSuggestions();
    }

    // ── FAB toggle ─────────────────────────────────────────────

    @FXML
    private void toggleChat() {
        if (panelOpen) closePanel();
        else           openPanel();
    }

    private void openPanel() {
        panelOpen = true;
        fabBtn.setText("✕");

        chatPanel.setVisible(true);
        chatPanel.setManaged(true);
        chatPanel.setTranslateY(30);
        chatPanel.setOpacity(0);

        // FadeTransition: pass node to constructor, then configure
        FadeTransition ft = new FadeTransition(Duration.millis(220), chatPanel);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), chatPanel);
        tt.setFromY(30);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
        Platform.runLater(inputField::requestFocus);
    }

    private void closePanel() {
        panelOpen = false;
        fabBtn.setText("💬");

        FadeTransition ft = new FadeTransition(Duration.millis(180), chatPanel);
        ft.setFromValue(1);
        ft.setToValue(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(180), chatPanel);
        tt.setFromY(0);
        tt.setToY(20);
        tt.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.setOnFinished(e -> {
            chatPanel.setVisible(false);
            chatPanel.setManaged(false);
        });
        pt.play();
    }

    // ── Keyboard shortcut (Enter to send) ─────────────────────

    private void setupInputKeyHandler() {
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                handleSend();
            }
        });
    }

    // ── Send message ───────────────────────────────────────────

    @FXML
    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isBlank()) return;

        inputField.clear();
        inputField.setDisable(true);
        sendBtn.setDisable(true);

        // Hide suggestion chips after first real message
        suggestionsRow.setVisible(false);
        suggestionsRow.setManaged(false);

        addUserBubble(text);

        // addTypingIndicator() returns the HBox row — the direct child of messagesArea
        HBox typingRow = addTypingIndicator();

        // Call AI on a daemon background thread
        Thread worker = new Thread(() -> {
            String response = chatService.chat(text);
            Platform.runLater(() -> {
                messagesArea.getChildren().remove(typingRow);
                addBotBubble(response);
                inputField.setDisable(false);
                sendBtn.setDisable(false);
                inputField.requestFocus();
                scrollToBottom();
            });
        });
        worker.setDaemon(true);
        worker.setName("espritbot-worker");
        worker.start();
    }

    // ── Clear conversation ─────────────────────────────────────

    @FXML
    private void handleClear() {
        chatService.reset();
        messagesArea.getChildren().clear();
        suggestionsRow.setVisible(true);
        suggestionsRow.setManaged(true);
        showWelcomeMessage();
        buildSuggestions();
    }

    // ── Bubble builders ────────────────────────────────────────

    private void addUserBubble(String text) {
        Label bubble = new Label(text);
        bubble.getStyleClass().add("msg-user");
        bubble.setWrapText(true);
        bubble.setMaxWidth(280);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(0, 0, 0, 40));

        animateBubble(row);
        messagesArea.getChildren().add(row);
        scrollToBottom();
    }

    private void addBotBubble(String text) {
        Label senderLbl = new Label("🤖 EspritBot");
        senderLbl.getStyleClass().add("msg-sender-label");

        VBox contentBox = renderMarkdown(text);

        VBox wrapper = new VBox(3, senderLbl, contentBox);
        wrapper.setAlignment(Pos.TOP_LEFT);

        HBox row = new HBox(wrapper);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 40, 0, 0));

        animateBubble(row);
        messagesArea.getChildren().add(row);
        scrollToBottom();
    }

    /**
     * Splits the AI response on triple-backtick fences and renders:
     *   - Even segments → styled text label (msg-bot)
     *   - Odd segments  → monospace code label with dark background
     */
    private VBox renderMarkdown(String text) {
        VBox box = new VBox(6);
        box.setMaxWidth(320);

        String[] parts = text.split("```");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) continue;

            if (i % 2 == 1) {
                // Code block — strip the language tag on the first line
                String[] lines = part.split("\n", 2);
                String code = lines.length > 1 ? lines[1] : lines[0];

                Label codeLbl = new Label(code);
                codeLbl.setStyle(
                    "-fx-font-family: 'Courier New', monospace;" +
                    "-fx-font-size: 11.5px;" +
                    "-fx-text-fill: #A5F3FC;" +
                    "-fx-background-color: rgba(0,0,0,0.45);" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: rgba(99,102,241,0.30);" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 8 10;"
                );
                codeLbl.setWrapText(true);
                codeLbl.setMaxWidth(300);
                box.getChildren().add(codeLbl);
            } else {
                Label textLbl = new Label(part);
                textLbl.getStyleClass().add("msg-bot");
                textLbl.setWrapText(true);
                textLbl.setMaxWidth(320);
                box.getChildren().add(textLbl);
            }
        }

        return box;
    }

    /**
     * Shows a pulsing "thinking…" indicator.
     * Returns the HBox row (direct child of messagesArea) so the caller can remove it.
     */
    private HBox addTypingIndicator() {
        Label lbl = new Label("⏳ EspritBot réfléchit...");
        lbl.getStyleClass().add("msg-typing");
        lbl.setWrapText(true);

        FadeTransition pulse = new FadeTransition(Duration.millis(700), lbl);
        pulse.setFromValue(0.4);
        pulse.setToValue(1.0);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        HBox row = new HBox(lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        messagesArea.getChildren().add(row);
        scrollToBottom();
        return row;   // ← return the ROW, not the Label
    }

    // ── Welcome message ────────────────────────────────────────

    private void showWelcomeMessage() {
        String userName = "Étudiant";
        try {
            var u = SessionManager.getInstance().getCurrentUser();
            if (u != null && u.getPrenom() != null && !u.getPrenom().isBlank()) {
                userName = u.getPrenom();
            }
        } catch (Exception ignored) {}

        String welcome =
                "👋 Bonjour " + userName + " !\n\n" +
                "Je suis EspritBot, votre assistant académique IA.\n\n" +
                "Posez-moi n'importe quelle question sur vos cours : " +
                "algorithmique, programmation, bases de données, réseaux…\n\n" +
                "Je suis là pour vous aider ! 🎓";

        Label botLbl = new Label(welcome);
        botLbl.getStyleClass().add("msg-bot");
        botLbl.setWrapText(true);
        botLbl.setMaxWidth(310);

        Label senderLbl = new Label("🤖 EspritBot");
        senderLbl.getStyleClass().add("msg-sender-label");

        VBox wrapper = new VBox(3, senderLbl, botLbl);
        wrapper.setAlignment(Pos.TOP_LEFT);

        HBox row = new HBox(wrapper);
        row.setAlignment(Pos.CENTER_LEFT);
        messagesArea.getChildren().add(row);
    }

    // ── Suggestion chips ───────────────────────────────────────

    private void buildSuggestions() {
        if (suggestionsRow == null) return;
        suggestionsRow.getChildren().clear();

        VBox chipContainer = new VBox(6);
        chipContainer.setPadding(new Insets(4, 0, 0, 0));

        for (String suggestion : SUGGESTIONS) {
            Button chip = new Button(suggestion);
            chip.getStyleClass().add("suggestion-chip");
            chip.setWrapText(false);
            chip.setOnAction(e -> {
                // Strip leading emoji / symbol characters before first letter
                String cleaned = suggestion.replaceAll("^\\P{L}+", "").trim();
                inputField.setText(cleaned);
                handleSend();
            });
            chipContainer.getChildren().add(chip);
        }

        suggestionsRow.getChildren().add(chipContainer);
    }

    // ── Helpers ────────────────────────────────────────────────

    /** Slide-in + fade-in entrance animation for a bubble row. */
    private void animateBubble(HBox row) {
        row.setOpacity(0);
        row.setTranslateY(8);

        FadeTransition ft = new FadeTransition(Duration.millis(200), row);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), row);
        tt.setFromY(8);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> messagesScroll.setVvalue(1.0));
    }
}
