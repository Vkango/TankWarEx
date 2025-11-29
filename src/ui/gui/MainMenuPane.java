package ui.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MainMenuPane extends VBox {
    private Runnable onStartGame;
    private Runnable onExitGame;

    public MainMenuPane() {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setStyle("-fx-background-color: #2c3e50;");
        Text title = new Text("TankWarEx");
        title.setFont(Font.font("微软雅黑", 48));
        title.setFill(Color.web("#ecf0f1"));

        Button startBtn = createButton("Start Game");
        Button exitBtn = createButton("Exit Game");

        startBtn.setOnAction(e -> {
            if (onStartGame != null)
                onStartGame.run();
        });
        exitBtn.setOnAction(e -> {
            if (onExitGame != null)
                onExitGame.run();
        });

        getChildren().addAll(title, startBtn, exitBtn);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 24));
        btn.setPrefWidth(200);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        return btn;
    }

    public void setOnStartGame(Runnable handler) {
        this.onStartGame = handler;
    }

    public void setOnExitGame(Runnable handler) {
        this.onExitGame = handler;
    }
}