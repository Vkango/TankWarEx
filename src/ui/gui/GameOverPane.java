package ui.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameOverPane extends VBox {
    private Runnable onRestart;
    private Runnable onBackToLevelSelect;
    private Text messageText;

    public GameOverPane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        Text title = new Text("游戏结束");
        title.setFont(Font.font("微软雅黑", 48));
        title.setFill(Color.RED);

        messageText = new Text("");
        messageText.setFont(Font.font("微软雅黑", 24));
        messageText.setFill(Color.WHITE);

        Button restartBtn = createButton("重新开始");
        Button backBtn = createButton("返回");

        restartBtn.setOnAction(e -> {
            if (onRestart != null)
                onRestart.run();
        });
        backBtn.setOnAction(e -> {
            if (onBackToLevelSelect != null)
                onBackToLevelSelect.run();
        });

        getChildren().addAll(title, messageText, restartBtn, backBtn);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 20));
        btn.setPrefWidth(200);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        return btn;
    }

    public void setMessage(String message) {
        messageText.setText(message);
    }

    public void setOnRestart(Runnable handler) {
        this.onRestart = handler;
    }

    public void setOnBackToLevelSelect(Runnable handler) {
        this.onBackToLevelSelect = handler;
    }
}
