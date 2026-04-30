package piJava.Controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class CoursesAnimationHelpers {

    @FXML private VBox    heroSection;
    @FXML private Label   sectionLabel;
    @FXML private VBox    emptyState;
    @FXML private Rectangle loadingBarFill;
    @FXML private StackPane loadingBarPane;
    @FXML private Button  chipAll, chipDev, chipDesign, chipBusiness, chipData;
    @FXML private FlowPane coursesContainer;
    @FXML private javafx.scene.control.ScrollPane coursesScroll;

    // ─── Hero entrance animation ─────────────────────────────────────────────

    public void animateHeroEntrance() {
        if (heroSection == null) return;
        heroSection.setOpacity(0);
        heroSection.setTranslateY(-20);

        FadeTransition fade = new FadeTransition(Duration.millis(500), heroSection);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), heroSection);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }

    // ─── Shimmer loading bar ─────────────────────────────────────────────────

    private Timeline shimmerTimeline;

    public void startLoadingBar() {
        if (loadingBarFill == null || loadingBarPane == null) return;
        loadingBarFill.setWidth(0);
        loadingBarFill.setVisible(true);

        loadingBarPane.widthProperty().addListener((obs, o, n) ->
            loadingBarFill.setWidth(0));

        shimmerTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(loadingBarFill.widthProperty(), 0)),
            new KeyFrame(Duration.millis(1800),
                new KeyValue(loadingBarFill.widthProperty(),
                    loadingBarPane.getWidth(), Interpolator.EASE_BOTH))
        );
        shimmerTimeline.setCycleCount(Animation.INDEFINITE);
        shimmerTimeline.setAutoReverse(true);
        shimmerTimeline.play();
    }

    public void stopLoadingBar() {
        if (shimmerTimeline != null) shimmerTimeline.stop();
        if (loadingBarPane == null || loadingBarFill == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(300), loadingBarPane);
        ft.setToValue(0);
        ft.setOnFinished(e -> loadingBarFill.setVisible(false));
        ft.play();
    }

    // ─── Staggered card reveal ────────────────────────────────────────────────

    public void animateCardsIn() {
        if (coursesContainer == null) return;
        int[] index = {0};
        coursesContainer.getChildren().forEach(node -> {
            node.setOpacity(0);
            node.setTranslateY(24);

            FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition pt = new ParallelTransition(fade, slide);
            pt.setDelay(Duration.millis(index[0] * 70));
            pt.play();
            index[0]++;
        });
    }

    // ─── Card hover animations ────────────────────────────────────────────────

    public void wireCardHover(VBox card, Label arrowLabel) {
        DropShadow shadowHover = new DropShadow();
        shadowHover.setColor(Color.rgb(198, 40, 40, 0.22));
        shadowHover.setRadius(22);
        shadowHover.setOffsetY(8);

        DropShadow shadowDefault = new DropShadow();
        shadowDefault.setColor(Color.rgb(198, 40, 40, 0.06));
        shadowDefault.setRadius(12);
        shadowDefault.setOffsetY(4);

        card.setEffect(shadowDefault);

        card.setOnMouseEntered(e -> {
            TranslateTransition lift = new TranslateTransition(Duration.millis(220), card);
            lift.setToY(-6);
            lift.setInterpolator(Interpolator.EASE_OUT);
            lift.play();

            card.setEffect(shadowHover);

            if (arrowLabel != null) {
                TranslateTransition arrowSlide = new TranslateTransition(Duration.millis(200), arrowLabel);
                arrowSlide.setToX(5);
                arrowSlide.play();
            }
        });

        card.setOnMouseExited(e -> {
            TranslateTransition lower = new TranslateTransition(Duration.millis(220), card);
            lower.setToY(0);
            lower.setInterpolator(Interpolator.EASE_OUT);
            lower.play();

            card.setEffect(shadowDefault);

            if (arrowLabel != null) {
                TranslateTransition arrowBack = new TranslateTransition(Duration.millis(200), arrowLabel);
                arrowBack.setToX(0);
                arrowBack.play();
            }
        });

        card.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(100), card);
            press.setToX(0.97);
            press.setToY(0.97);
            press.play();
        });

        card.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(120), card);
            release.setToX(1.0);
            release.setToY(1.0);
            release.play();
        });
    }

    // ─── Thumbnail hover overlay ──────────────────────────────────────────────

    public void wireThumbnailOverlay(StackPane thumbnail, Pane overlay) {
        overlay.setOpacity(0);

        thumbnail.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(220), overlay);
            ft.setToValue(1);
            ft.play();

            ScaleTransition scale = new ScaleTransition(Duration.millis(300), thumbnail);
            scale.setToX(1.06);
            scale.setToY(1.06);
            scale.play();
        });

        thumbnail.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(220), overlay);
            ft.setToValue(0);
            ft.play();

            ScaleTransition scale = new ScaleTransition(Duration.millis(300), thumbnail);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    // ─── Chip toggle helper ───────────────────────────────────────────────────

    public void switchChip(Button selected) {
        if (chipAll == null) return;
        Button[] chips = {chipAll, chipDev, chipDesign, chipBusiness, chipData};
        for (Button c : chips) {
            if (c == null) continue;
            c.getStyleClass().remove("chip-active");
            if (!c.getStyleClass().contains("chip")) c.getStyleClass().add("chip");
        }
        selected.getStyleClass().remove("chip");
        if (!selected.getStyleClass().contains("chip-active"))
            selected.getStyleClass().add("chip-active");
    }

    // ─── Section label updater ────────────────────────────────────────────────

    public void setSectionLabel(String text) {
        if (sectionLabel == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(200), sectionLabel);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            sectionLabel.setText(text.toUpperCase());
            FadeTransition ft2 = new FadeTransition(Duration.millis(200), sectionLabel);
            ft2.setToValue(1);
            ft2.play();
        });
        ft.play();
    }

    // ─── Empty state toggle ───────────────────────────────────────────────────

    public void showEmptyState(boolean show) {
        if (emptyState == null || coursesScroll == null) return;
        emptyState.setVisible(show);
        emptyState.setManaged(show);
        coursesScroll.setVisible(!show);
        coursesScroll.setManaged(!show);

        if (show) {
            emptyState.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(300), emptyState);
            ft.setToValue(1);
            ft.play();
        }
    }
}
