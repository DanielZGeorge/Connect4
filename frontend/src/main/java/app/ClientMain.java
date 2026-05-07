package app;

import ui.LoginView;
import ui.MainMenuView;
import ui.GameBoardView;
import ui.LeaderboardView;
import ui.BotDifficultyView;
import ui.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) {
        ViewManager.setStage(stage);

        // Initialize UI Components
        LoginView    loginView = new LoginView();
        MainMenuView menuView  = new MainMenuView();
        GameBoardView boardView = new GameBoardView();
        LeaderboardView leaderboardView = new LeaderboardView();
        BotDifficultyView botDifficultyView = new BotDifficultyView();

        // Register Scenes
        ViewManager.addScene("Login", loginView.getScene());
        ViewManager.addScene("Menu",  menuView.getScene());
        ViewManager.addScene("Game",  boardView.getScene());
        ViewManager.addScene("Leaderboard", leaderboardView.getScene());
        ViewManager.addScene("BotDifficulty", botDifficultyView.getScene());

        // Start at Login
        stage.setTitle("Connect 4 Online");
        ViewManager.switchTo("Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
