package ui;

import client.HttpClientUtil;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegistrationView {

    private Scene scene;

    public RegistrationView() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 40;");

        Label title = new Label("Connect 4 Online");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);

        Button registerBtn = new Button("Register");
        registerBtn.setMinWidth(200);
        registerBtn.setOnAction(e -> {
            String user = usernameField.getText();
            String email = emailField.getText();
            String pass = passwordField.getText();

            //testing response of login servlets 
            String response = HttpClientUtil.register(user, email, pass);
            if ("REGISTER_SUCCESS".equals(response)) {
                System.out.println("Successfully registered!");
                ViewManager.setUsername(user);
                ViewManager.setGuest(false);
                ViewManager.switchTo("Menu");
            } else if ("USERNAME_OR_EMAIL_TAKEN".equals(response)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Username or email is taken");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error");
                alert.showAndWait();
            }
        });

        layout.getChildren().addAll(title, usernameField, emailField, passwordField, registerBtn);
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
