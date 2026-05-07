package ui;

import client.WsGameClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class BotDifficultyView {
    private final Scene scene;

    public BotDifficultyView() {
        VBox layout = new VBox(16);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #34495e; -fx-padding: 40;");

        Label title = new Label("Choose Bot Difficulty");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Button easyBtn = new Button("Easy Bot");
        Button mediumBtn = new Button("Medium Bot");
        Button hardBtn = new Button("Hard Bot");
        Button backBtn = new Button("Back to Menu");

        easyBtn.setMinWidth(220);
        mediumBtn.setMinWidth(220);
        hardBtn.setMinWidth(220);

        easyBtn.setOnAction(e -> requestBot("easy"));
        mediumBtn.setOnAction(e -> requestBot("medium"));
        hardBtn.setOnAction(e -> requestBot("hard"));
        backBtn.setOnAction(e -> ViewManager.switchTo("Menu"));

        layout.getChildren().addAll(title, easyBtn, mediumBtn, hardBtn, backBtn);
        scene = new Scene(layout, 800, 600);
    }

    public Scene getScene() {
        return scene;
    }

    private void requestBot(String difficulty) {
        if (ViewManager.isGuest()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Guests can only spectate matches.");
            alert.showAndWait();
            return;
        }

        String username = ViewManager.getUsername();
        WsGameClient client = WsGameClient.getInstance();
        client.connectIfNeeded(username, false);
        client.send("BOT;" + username + ";server;difficulty=" + difficulty);
    }
}

