package ui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;

public class ViewManager {
    private static Stage primaryStage;
    private static HashMap<String, Scene> scenes = new HashMap<>();
    private static String username;
    private static boolean guest;
    private static String currentSceneName;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void addScene(String name, Scene scene) {
        scenes.put(name, scene);
    }

    public static void switchTo(String name) {
        if (scenes.containsKey(name)) {
            primaryStage.setScene(scenes.get(name));
            primaryStage.show();
            currentSceneName = name;
        } else {
            System.out.println("Scene " + name + " not found!");
        }
    }

    public static String getCurrentSceneName() {
        return currentSceneName;
    }

    public static void setUsername(String user) {
        username = user;
    }

    public static String getUsername() {
        return username;
    }

    public static void setGuest(boolean isGuest) {
        guest = isGuest;
    }

    public static boolean isGuest() {
        return guest;
    }
}