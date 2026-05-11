package ui;

import client.HttpClientUtil;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
        registerBtn.setStyle("-fx-background-color: #3b6fff; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> showRegisterDialog(usernameField, passwordField));

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

    private void showRegisterDialog(TextField loginUsernameField, PasswordField loginPasswordField) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Account");
        dialog.setHeaderText("Register a new Connect 4 account");

        ButtonType createBtn = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        TextField username = new TextField();
        username.setPromptText("Username");
        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirm password");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff6464;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Username:"), 0, 0); grid.add(username, 1, 0);
        grid.add(new Label("Email:"), 0, 1);    grid.add(email, 1, 1);
        grid.add(new Label("Password:"), 0, 2); grid.add(password, 1, 2);
        grid.add(new Label("Confirm:"), 0, 3);  grid.add(confirm, 1, 3);
        grid.add(errorLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createBtn);
        createButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String u = username.getText() == null ? "" : username.getText().trim();
            String em = email.getText() == null ? "" : email.getText().trim();
            String p = password.getText() == null ? "" : password.getText();
            String c = confirm.getText() == null ? "" : confirm.getText();

            if (u.isEmpty() || em.isEmpty() || p.isEmpty()) {
                errorLabel.setText("All fields are required.");
                ev.consume();
                return;
            }
            if (!p.equals(c)) {
                errorLabel.setText("Passwords do not match.");
                ev.consume();
                return;
            }

            String response = HttpClientUtil.register(u, em, p);
            switch (response == null ? "" : response) {
                case "REGISTER_SUCCESS":
                    loginUsernameField.setText(u);
                    loginPasswordField.setText(p);
                    new Alert(Alert.AlertType.INFORMATION, "Account created! You can log in now.").showAndWait();
                    break;
                case "USERNAME_OR_EMAIL_TAKEN":
                    errorLabel.setText("Username or email already taken.");
                    ev.consume();
                    break;
                default:
                    errorLabel.setText("Registration failed. Try again.");
                    ev.consume();
            }
        });

        dialog.showAndWait();
    }
}
