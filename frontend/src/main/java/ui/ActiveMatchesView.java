package ui;

import client.ActiveMatch;
import client.HttpClientUtil;
import client.WsGameClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class ActiveMatchesView {
    private final ObservableList<ActiveMatch> matches = FXCollections.observableArrayList();
    private final TableView<ActiveMatch> table = new TableView<>();
    private final Label statusLabel = new Label("");

    private final Scene scene;

    public ActiveMatchesView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20;");

        root.setTop(buildHeader());
        root.setCenter(buildTable());
        root.setBottom(buildFooter());

        scene = new Scene(root, 900, 650);
        refresh();
    }

    public Scene getScene() {
        return scene;
    }

    public void refresh() {
        statusLabel.setText("Loading...");
        Task<List<ActiveMatch>> task = new Task<>() {
            @Override
            protected List<ActiveMatch> call() {
                return HttpClientUtil.getActiveMatches();
            }
        };
        task.setOnSucceeded(e -> {
            List<ActiveMatch> result = task.getValue();
            matches.setAll(result);
            statusLabel.setText(result.isEmpty() ? "No active matches right now." : (result.size() + " active match" + (result.size() == 1 ? "" : "es")));
        });
        task.setOnFailed(e -> {
            matches.clear();
            statusLabel.setText("Couldn't load matches.");
        });
        new Thread(task, "active-matches-loader").start();
    }

    private VBox buildHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Active Matches");
        title.setStyle("-fx-fill: #e8eaf0; -fx-font-size: 24px; -fx-font-weight: bold;");

        Text subtitle = new Text("Pick a match to spectate live.");
        subtitle.setStyle("-fx-fill: #b8becc; -fx-font-size: 13px;");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        Button backBtn = new Button(ViewManager.isGuest() ? "Back to Login" : "Back to Menu");
        backBtn.setOnAction(e -> ViewManager.switchTo(ViewManager.isGuest() ? "Login" : "Menu"));

        HBox controls = new HBox(8, refreshBtn, backBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(title, subtitle, controls);
        return header;
    }

    private TableView<ActiveMatch> buildTable() {
        table.setItems(matches);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No active matches right now."));

        TableColumn<ActiveMatch, String> p1Col = new TableColumn<>("Player 1");
        p1Col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPlayer1()));

        TableColumn<ActiveMatch, String> vsCol = new TableColumn<>("");
        vsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("vs"));
        vsCol.setMaxWidth(60);

        TableColumn<ActiveMatch, String> p2Col = new TableColumn<>("Player 2");
        p2Col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPlayer2()));

        TableColumn<ActiveMatch, String> idCol = new TableColumn<>("Match ID");
        idCol.setCellValueFactory(data -> {
            String id = data.getValue().getGameId();
            return new javafx.beans.property.SimpleStringProperty(id.length() > 8 ? id.substring(0, 8) : id);
        });

        table.getColumns().addAll(p1Col, vsCol, p2Col, idCol);

        // Double-click to spectate
        table.setRowFactory(tv -> {
            TableRow<ActiveMatch> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    spectate(row.getItem());
                }
            });
            return row;
        });
        return table;
    }

    private VBox buildFooter() {
        VBox footer = new VBox(8);
        footer.setPadding(new Insets(10, 0, 0, 0));

        Button spectateBtn = new Button("Spectate Selected");
        spectateBtn.setStyle("-fx-background-color: #3b6fff; -fx-text-fill: white;");
        spectateBtn.setOnAction(e -> {
            ActiveMatch selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                spectate(selected);
            } else {
                statusLabel.setText("Pick a match first.");
            }
        });

        statusLabel.setStyle("-fx-text-fill: #b8becc; -fx-font-size: 12px;");

        HBox row = new HBox(10, spectateBtn, statusLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().add(row);
        return footer;
    }

    private void spectate(ActiveMatch match) {
        String username = ViewManager.getUsername();
        if (username == null || username.isBlank()) {
            username = "guest_" + System.currentTimeMillis();
            ViewManager.setUsername(username);
            ViewManager.setGuest(true);
        }
        WsGameClient ws = WsGameClient.getInstance();
        ws.connectIfNeeded(username, ViewManager.isGuest());
        ViewManager.setSpectating(true);
        // Server expects: SPECTATE;<self>;<targetPlayerUsername>;watch
        ws.send("SPECTATE;" + username + ";" + match.getPlayer1() + ";watch");
        // GameBoardView's STATE handler will auto-switch to the Game scene.
        Platform.runLater(() -> statusLabel.setText("Spectating " + match.getPlayer1() + " vs " + match.getPlayer2() + "..."));
    }
}
