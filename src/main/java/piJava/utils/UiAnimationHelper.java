package piJava.utils;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Locale;

public final class UiAnimationHelper {

    private UiAnimationHelper() {
    }

    public static void playStaggeredEntrance(Node... nodes) {
        if (nodes == null) {
            return;
        }
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (node == null) {
                continue;
            }
            playEntrance(node, Duration.millis(i * 90L));
        }
    }

    public static void playEntrance(Node node, Duration delay) {
        if (node == null) {
            return;
        }

        node.setOpacity(0);
        node.setTranslateY(18);
        node.setScaleX(0.96);
        node.setScaleY(0.96);

        FadeTransition fade = new FadeTransition(Duration.millis(420), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition translate = new TranslateTransition(Duration.millis(420), node);
        translate.setFromY(18);
        translate.setToY(0);
        translate.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scale = new ScaleTransition(Duration.millis(420), node);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(fade, translate, scale);
        SequentialTransition sequence = new SequentialTransition(new PauseTransition(delay), entrance);
        sequence.play();
    }

    public static void animateInteger(Label label, int from, int to, Duration duration, String suffix) {
        if (label == null) {
            return;
        }
        IntegerProperty value = new SimpleIntegerProperty(from);
        value.addListener((obs, oldValue, newValue) -> label.setText(newValue.intValue() + suffix));
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(value, from)),
                new KeyFrame(duration, new KeyValue(value, to, Interpolator.EASE_OUT))
        );
        timeline.play();
    }

    public static void animateDouble(Label label, double from, double to, Duration duration, int decimals, String suffix) {
        if (label == null) {
            return;
        }
        DoubleProperty value = new SimpleDoubleProperty(from);
        value.addListener((obs, oldValue, newValue) ->
                label.setText(String.format(Locale.US, "%1$." + decimals + "f%s", newValue.doubleValue(), suffix)));
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(value, from)),
                new KeyFrame(duration, new KeyValue(value, to, Interpolator.EASE_OUT))
        );
        timeline.play();
    }
}

