package ui;

import client.HttpClientUtil;
import client.LeaderboardEntry;
import client.LeaderboardResponse;
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

public class LeaderboardView {
    private static final int PAGE_SIZE = 20;

    private final ObservableList<LeaderboardEntry> entries = FXCollections.observableArrayList();
    private final TableView<LeaderboardEntry> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final Label pageLabel = new Label("Page 1");
    private final Label totalLabel = new Label("Total: 0");
    private final Button prevButton = new Button("Prev");
    private final Button nextButton = new Button("Next");

    private int currentPage = 1;
    private int total = 0;

    private final Scene scene;

    public LeaderboardView() {
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

    private VBox buildHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Leaderboard");
        title.setStyle("-fx-fill: #e8eaf0; -fx-font-size: 24px; -fx-font-weight: bold;");

        searchField.setPromptText("Search players...");
        searchField.setMaxWidth(260);
        searchField.setOnAction(e -> {
            currentPage = 1;
            refresh();
        });

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> {
            currentPage = 1;
            refresh();
        });

        Button backBtn = new Button("Back to Menu");
        backBtn.setOnAction(e -> ViewManager.switchTo("Menu"));

        HBox searchRow = new HBox(8, searchField, searchBtn, backBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(title, searchRow);
        return header;
    }

    private TableView<LeaderboardEntry> buildTable() {
        table.setItems(entries);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No players found"));

        TableColumn<LeaderboardEntry, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<LeaderboardEntry, String> eloCol = new TableColumn<>("Elo");
        eloCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getElo())));

        TableColumn<LeaderboardEntry, String> matchCol = new TableColumn<>("Status");
        matchCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isInMatch() ? "In Match" : "Idle"));

        table.getColumns().addAll(nameCol, eloCol, matchCol);
        return table;
    }

    private VBox buildFooter() {
        VBox footer = new VBox(8);
        footer.setPadding(new Insets(10, 0, 0, 0));

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                refresh();
            }
        });

        nextButton.setOnAction(e -> {
            int maxPage = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            if (currentPage < maxPage) {
                currentPage++;
                refresh();
            }
        });

        HBox row = new HBox(10, prevButton, pageLabel, nextButton, totalLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().add(row);
        return footer;
    }

    private void refresh() {
        String query = searchField.getText();
        Task<LeaderboardResponse> task = new Task<>() {
            @Override
            protected LeaderboardResponse call() {
                return HttpClientUtil.getLeaderboard(query, currentPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            LeaderboardResponse response = task.getValue();
            List<LeaderboardEntry> newEntries = response.getEntries();
            total = response.getTotal();

            entries.setAll(newEntries);
            pageLabel.setText("Page " + currentPage);
            totalLabel.setText("Total: " + total);

            int maxPage = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= maxPage);
        });

        task.setOnFailed(e -> {
            entries.clear();
            total = 0;
            pageLabel.setText("Page " + currentPage);
            totalLabel.setText("Total: 0");
        });

        new Thread(task, "leaderboard-loader").start();
    }
}

