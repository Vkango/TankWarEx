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

    public LevelSelectPane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setStyle("-fx-background-color: #34495e;");

        Text title = new Text("Select a map!");
        title.setFont(Font.font("微软雅黑", 36));
        title.setFill(Color.WHITE);

        VBox levelList = new VBox(10);
        levelList.setAlignment(Pos.CENTER);

        for (MapProvider map : PluginManager.getInstance().getMapProviders()) {
            Button levelBtn = createLevelButton(map.getMapName(), map);
            levelList.getChildren().add(levelBtn);
        }

        Button backBtn = createButton("Back to Main Menu");
        backBtn.setOnAction(e -> {
            if (onBack != null)
                onBack.run();
        });

        getChildren().addAll(title, levelList, backBtn);
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