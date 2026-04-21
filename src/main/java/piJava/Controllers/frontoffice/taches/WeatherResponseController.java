package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import piJava.services.api.WeatherAiService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherResponseController {

    @FXML
    private Label weatherDescription;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label humidityLabel;

    @FXML
    private Label windLabel;

    @FXML
    private VBox aiAdviceContainer;

    @FXML
    private Label timestampLabel;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        updateTimestamp();
    }


    /**
     * Set the complete weather and AI response
     * This expects a full response with weather data and AI advice
     */
    public void setResponse(String response) {
        if (response != null && !response.trim().isEmpty()) {
            parseAndDisplayResponse(response);
        }
        updateTimestamp();
    }

    /**
     * Set weather data separately
     */
    public void setWeatherData(String description, double temperature, int humidity, double wind) {
        if (weatherDescription != null) {
            weatherDescription.setText(description != null ? description : "Données météo");
        }
        if (temperatureLabel != null) {
            temperatureLabel.setText(String.format("%.2f°C", temperature));
        }
        if (humidityLabel != null) {
            humidityLabel.setText(humidity + "%");
        }
        if (windLabel != null) {
            windLabel.setText(String.format("%.1f km/h", wind * 3.6)); // converting m/s to km/h
        }
        updateTimestamp();
    }

    /**
     * Set weather data with string values
     */
    public void setWeatherData(String description, String temperature, String humidity, String wind) {
        if (weatherDescription != null) {
            weatherDescription.setText(description != null ? description : "Données météo");
        }
        if (temperatureLabel != null) {
            temperatureLabel.setText(temperature != null ? temperature : "--°C");
            if (temperature != null && !temperature.contains("°C")) {
                temperatureLabel.setText(temperature + "°C");
            }
        }
        if (humidityLabel != null) {
            humidityLabel.setText(humidity != null ? humidity : "--%");
            if (humidity != null && !humidity.contains("%")) {
                humidityLabel.setText(humidity + "%");
            }
        }
        if (windLabel != null) {
            windLabel.setText(wind != null ? wind : "-- km/h");
        }
        updateTimestamp();
    }

    /**
     * Set only the AI advice content
     */
    public void setAIAdvice(String advice) {
        if (aiAdviceContainer != null && advice != null && !advice.trim().isEmpty()) {
            aiAdviceContainer.getChildren().clear();
            parseAndDisplayAdvice(advice);
        }
    }

    /**
     * Parse the complete response to extract weather data and AI advice
     */
    private void parseAndDisplayResponse(String response) {
        if (aiAdviceContainer == null || response == null) {
            return;
        }

        // Clear previous content
        aiAdviceContainer.getChildren().clear();

        // Parse and display the advice
        parseAndDisplayAdvice(response);
    }

    /**
     * Parse AI advice and create beautiful card items
     */
    private void parseAndDisplayAdvice(String advice) {
        // Split by lines
        String[] lines = advice.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Extract emoji from the start
            String emoji = extractLeadingEmoji(line);
            String content = line;

            // Remove emoji from content if it was at the start
            if (!emoji.isEmpty()) {
                content = content.substring(emoji.length()).trim();
            }

            // Split by colon to get title and description
            String title = "";
            String description = "";

            if (content.contains(":")) {
                int colonIndex = content.indexOf(":");
                title = content.substring(0, colonIndex).trim();
                description = content.substring(colonIndex + 1).trim();
            } else {
                // No colon, treat entire content as description
                description = content;
            }

            // Create and add the advice card
            VBox adviceCard = createAdviceCard(emoji, title, description);
            aiAdviceContainer.getChildren().add(adviceCard);
        }
    }

    /**
     * Create a beautiful advice card with icon, title, and description
     */
    private VBox createAdviceCard(String emoji, String title, String description) {
        // Main container
        VBox card = new VBox(12);
        card.getStyleClass().add("advice-item");

        // Header with emoji and title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("advice-header");

        // Emoji label
        Label emojiLabel = new Label(emoji.isEmpty() ? "💡" : emoji);
        emojiLabel.getStyleClass().add("advice-icon");

        // Title label (only add if title exists)
        if (!title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("advice-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

            header.getChildren().addAll(emojiLabel, titleLabel);
        } else {
            header.getChildren().add(emojiLabel);
        }

        // Description label
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("advice-text");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        // Add header and description to card
        card.getChildren().add(header);
        if (!description.isEmpty()) {
            card.getChildren().add(descLabel);
        }

        return card;
    }

    /**
     * Extract emoji from the beginning of a string
     * Handles various emoji formats including compound emojis
     */
    private String extractLeadingEmoji(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Common emojis pattern - matches most emoji characters
        Pattern emojiPattern = Pattern.compile("^[\\p{So}\\p{Cn}\\u200D\\uFE0F]+");
        Matcher matcher = emojiPattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        // Fallback: Check if first character looks like an emoji (high Unicode range)
        char firstChar = text.charAt(0);
        if (Character.isHighSurrogate(firstChar) ||
                (firstChar >= 0x1F000 && firstChar <= 0x1FFFF) ||
                (firstChar >= 0x2600 && firstChar <= 0x27BF)) {

            // For surrogate pairs, take two characters
            if (Character.isHighSurrogate(firstChar) && text.length() > 1) {
                return text.substring(0, 2);
            }
            return String.valueOf(firstChar);
        }

        // Common single-character emojis
        String firstCharStr = String.valueOf(firstChar);
        if (firstCharStr.matches("[☀️🌤️⛅☁️🌧️⛈️🌩️🌨️❄️💧💨🌪️🌈⚡🔥💡📚👕⚡🏥🍎☕🎯⚽🧘😴🏃]")) {
            return firstCharStr;
        }

        return "";
    }

    /**
     * Update the timestamp label with current date and time in French format
     */
    private void updateTimestamp() {
        if (timestampLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            timestampLabel.setText("Mis à jour: " + now.format(formatter));
        }
    }

    /**
     * Handle close button action
     */
    @FXML
    private void handleClose() {
        if (timestampLabel != null && timestampLabel.getScene() != null &&
                timestampLabel.getScene().getWindow() != null) {
            timestampLabel.getScene().getWindow().hide();
        }
    }
}