package ui.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import game.map.MapProvider;
import plugin.api.PluginManager;
import java.util.function.Consumer;

public class LevelSelectPane extends VBox {
    private Runnable onBack;
    private Consumer<MapProvider> onLevelSelected;
    private VBox levelList;

    public LevelSelectPane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setStyle("-fx-background-color: #34495e;");

        Text title = new Text("选择一个地图");
        title.setFont(Font.font("微软雅黑", 36));
        title.setFill(Color.WHITE);

        levelList = new VBox(10);
        levelList.setAlignment(Pos.CENTER);
        refreshMapList();

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button reloadBtn = createButton("重新加载地图");
        reloadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        reloadBtn.setOnAction(e -> reloadMaps());

        Button backBtn = createButton("返回");
        backBtn.setOnAction(e -> {
            if (onBack != null)
                onBack.run();
        });

        buttonBox.getChildren().addAll(reloadBtn, backBtn);

        getChildren().addAll(title, levelList, buttonBox);
    }

    private void refreshMapList() {
        levelList.getChildren().clear();

        for (MapProvider map : PluginManager.getInstance().getMapProviders()) {
            Button levelBtn = createLevelButton(map.getMapName(), map);
            levelList.getChildren().add(levelBtn);
        }
    }

    private void reloadMaps() {
        System.out.println("[LevelSelectPane] Reloading plugins...");
        PluginManager.getInstance().loadPlugins();
        refreshMapList();
        System.out.println("[LevelSelectPane] Plugins reloaded successfully!");
    }

    private Button createLevelButton(String text, MapProvider map) {
        Button btn = new Button(text);
        btn.setFont(Font.font("微软雅黑", 20));
        btn.setPrefWidth(250);
        btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btn.setOnAction(e -> {
            if (onLevelSelected != null) {
                onLevelSelected.accept(map);
            }
        });
        return btn;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("微软雅黑", 20));
        btn.setPrefWidth(200);
        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        return btn;
    }

    public void setOnBack(Runnable handler) {
        this.onBack = handler;
    }

    public void setOnLevelSelected(Consumer<MapProvider> handler) {
        this.onLevelSelected = handler;
    }
}