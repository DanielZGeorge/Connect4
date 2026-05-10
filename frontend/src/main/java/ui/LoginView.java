package ui;

import mock.MockBackend;
import client.HttpClientUtil;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView {
    private Scene scene;

    public LoginView() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 40;");

        Label title = new Label("Connect 4 Online");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);

        Button loginBtn = new Button("Login");
        loginBtn.setMinWidth(200);
        loginBtn.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();

            //testing response of login servlets 
            String response = HttpClientUtil.login(user, pass);
            if ("LOGIN_SUCCESS".equals(response)) {
                System.out.println("Login Success!");
                ViewManager.setUsername(user);
                ViewManager.setGuest(false);
                ViewManager.switchTo("Menu");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Credentials!");
                alert.showAndWait();
            }
        });

        Button registerBtn = new Button("Register");
        registerBtn.setMinWidth(200);
        registerBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> {
            ViewManager.switchTo("Register");
        });

        Button guestBtn = new Button("Continue as Guest");
        guestBtn.setMinWidth(200);
        guestBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        guestBtn.setOnAction(e -> {
            ViewManager.setUsername("guest_" + System.currentTimeMillis());
            ViewManager.setGuest(true);
            ViewManager.switchTo("Menu");
        });

        layout.getChildren().addAll(title, usernameField, passwordField, loginBtn, registerBtn, guestBtn);
        this.scene = new Scene(layout, 800, 600);

        // Apply the CSS file to the scene
        try {
            java.net.URL cssUrl = getClass().getResource("/style.css");
            if (cssUrl == null) {
                // Fallback for IDEs not copying resources
                java.io.File cssFile = new java.io.File("Frontend/style.css");
                if (cssFile.exists()) {
                    cssUrl = cssFile.toURI().toURL();
                }
            }
            if (cssUrl != null) {
                this.scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Could not load style.css: File not found");
            }
        } catch (Exception ex) {
            System.out.println("Could not load style.css");
        }
    }

    public Scene getScene() { return scene; }
}
