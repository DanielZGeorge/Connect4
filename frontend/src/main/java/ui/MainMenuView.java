package ui;

import client.GameClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MainMenuView {
    private Scene scene;
    private GameClient client = new GameClient();

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
        
        //setting button actions
        playRankedBtn.setOnAction(e -> {
            try {
            	if (client == null) {
            	    client = new GameClient();
            	    client.connect();
            	    client.listen();
            	}
                String username = ViewManager.getUsername();
                client.sendMessage("MATCH;" + username + ";none;none");
                System.out.println("Sent ranked queue request");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        playBotBtn.setOnAction(e -> {
            try {
            	if (client == null) {
            	    client = new GameClient();
            	    client.connect();
            	    client.listen();
            	}
                client.sendMessage("PLAY_BOT");
                System.out.println("Sent bot request");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        logoutBtn.setOnAction(e -> ViewManager.switchTo("Login"));

        layout.getChildren().addAll(welcomeLabel, playRankedBtn, playBotBtn, leaderboardBtn, logoutBtn);
        this.scene = new Scene(layout, 800, 600);
    }

    public Scene getScene() { return scene; }
}
