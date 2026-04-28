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

        // Register Scenes
        ViewManager.addScene("Login", loginView.getScene());
        ViewManager.addScene("Menu",  menuView.getScene());
        ViewManager.addScene("Game",  boardView.getScene());

        // Start at Login
        stage.setTitle("Connect 4 Online");
        ViewManager.switchTo("Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
