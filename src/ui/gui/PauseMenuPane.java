package ui.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class PauseMenuPane extends VBox {
    private Runnable onResume;
    private Runnable onRestart;
    private Runnable onBackToLevelSelect;
    private Runnable onSaveGame;

    public PauseMenuPane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        Text title = new Text("Game Paused");
        title.setFont(Font.font("微软雅黑", 36));
        title.setFill(Color.WHITE);

        Button resumeBtn = createButton("继续游戏");
        Button saveBtn = createButton("保存游戏");
        Button restartBtn = createButton("重新开始");
        Button backBtn = createButton("返回选择关卡");

        resumeBtn.setOnAction(e -> {
            if (onResume != null)
                onResume.run();
        });
        saveBtn.setOnAction(e -> {
            if (onSaveGame != null)
                onSaveGame.run();
        });
        restartBtn.setOnAction(e -> {
            if (onRestart != null)
                onRestart.run();
        });
        backBtn.setOnAction(e -> {
            if (onBackToLevelSelect != null)
                onBackToLevelSelect.run();
        });

        getChildren().addAll(title, resumeBtn, saveBtn, restartBtn, backBtn);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 20));
        btn.setPrefWidth(200);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        return btn;
    }

    public void setOnResume(Runnable handler) {
        this.onResume = handler;
    }

    public void setOnRestart(Runnable handler) {
        this.onRestart = handler;
    }

    public void setOnBackToLevelSelect(Runnable handler) {
        this.onBackToLevelSelect = handler;
    }

    public void setOnSaveGame(Runnable handler) {
        this.onSaveGame = handler;
    }
}