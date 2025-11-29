package ui.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;

import javafx.scene.layout.Region;

public class Toast extends StackPane {
    public Toast(String message) {
        Label label = new Label(message);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 14));
        
        setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.8), new CornerRadii(20), Insets.EMPTY)));
        setPadding(new Insets(8, 16, 8, 16));
        getChildren().add(label);
        
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        setOpacity(0);
        setMouseTransparent(true);
    }
    
    public void show() {
        Timeline timeline = new Timeline();
        KeyFrame fadeIn = new KeyFrame(Duration.millis(300), new KeyValue(opacityProperty(), 1));
        KeyFrame stay = new KeyFrame(Duration.millis(2000), new KeyValue(opacityProperty(), 1));
        KeyFrame fadeOut = new KeyFrame(Duration.millis(2500), new KeyValue(opacityProperty(), 0));
        
        timeline.getKeyFrames().addAll(fadeIn, stay, fadeOut);
        timeline.setOnFinished(e -> {
            if (getParent() instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane)getParent()).getChildren().remove(this);
            }
        });
        timeline.play();
    }
}
