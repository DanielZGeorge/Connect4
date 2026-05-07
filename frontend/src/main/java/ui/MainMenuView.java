package ui;

import client.WsGameClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MainMenuView {
    private Scene scene;
    private final WsGameClient wsClient = WsGameClient.getInstance();

    public MainMenuView() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #34495e;");

        Label welcomeLabel = new Label("Welcome to the Lobby");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        Button playRankedBtn = new Button("Play Ranked Match");
        Button playBotBtn = new Button("Practice vs Bot");
        Button leaderboardBtn = new Button("View Leaderboard");
        Button logoutBtn = new Button("Logout");

        // Set widths for consistency
        playRankedBtn.setMinWidth(250);
        playBotBtn.setMinWidth(250);
        leaderboardBtn.setMinWidth(250);

        playRankedBtn.setOnAction(e -> {
            if (ViewManager.isGuest()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Guests can only spectate matches.");
                alert.showAndWait();
                return;
            }
            String username = ViewManager.getUsername();
            wsClient.connectIfNeeded(username, false);
            wsClient.send("MATCH;" + username + ";server;ranked");
        });

        playBotBtn.setOnAction(e -> ViewManager.switchTo("BotDifficulty"));
        leaderboardBtn.setOnAction(e -> ViewManager.switchTo("Leaderboard"));
        logoutBtn.setOnAction(e -> ViewManager.switchTo("Login"));

        layout.getChildren().addAll(welcomeLabel, playRankedBtn, playBotBtn, leaderboardBtn, logoutBtn);
        this.scene = new Scene(layout, 800, 600);
    }

    public Scene getScene() { return scene; }
}
