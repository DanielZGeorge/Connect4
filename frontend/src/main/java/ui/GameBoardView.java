package ui;

import client.GameClient;
//import client.NetworkMessage; // if needed later
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameBoardView {
	//match specifics
	private GameClient client;
	private String username;
	private String opponent;

    // ── Constants ────────────────────────────────────────────────────────────
    private static final int COLS        = 7;
    private static final int ROWS        = 6;
    private static final int CELL_RADIUS = 30;   // circle radius in px
    private static final int CELL_GAP    = 8;
    private static final int TURN_SECS   = 30;

    private static final Color COLOR_EMPTY  = Color.web("#1a1f32");
    private static final Color COLOR_P1     = Color.web("#ff3b3b");
    private static final Color COLOR_P2     = Color.web("#f5c400");
    private static final Color COLOR_BG     = Color.web("#2c3e50");
    private static final Color COLOR_BOARD  = Color.web("#12151f");
    private static final Color COLOR_PANEL  = Color.web("#1e2535");
    private static final Color COLOR_TEXT   = Color.web("#e8eaf0");
    private static final Color COLOR_MUTED  = Color.web("#5a607a");

    // ── Game state ───────────────────────────────────────────────────────────
    private int[][]   board       = new int[ROWS][COLS];
    private int       currentPlayer = 1;
    private boolean   gameOver    = false;
    private int       timeLeft    = TURN_SECS;
    private Timeline  timer;

    // ── Player info (inject from session/matchmaking later) ──────────────────
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private int    player1Elo  = 1200;
    private int    player2Elo  = 1150;

    // ── UI refs ──────────────────────────────────────────────────────────────
    private Scene     scene;
    private Circle[][]cells       = new Circle[ROWS][COLS];
    private Label     timerLabel;
    private ProgressBar timerBar;
    private Label     statusLabel;
    private VBox      chatMessages;
    private ScrollPane chatScroll;
    private TextField chatInput;
    private Button    chatSend;
    private Label     p1TurnDot;
    private Label     p2TurnDot;
    private VBox      p1Card;
    private VBox      p2Card;
    private GridPane  boardGrid;
    private VBox      postGameButtons;

    // ── Constructor ──────────────────────────────────────────────────────────
    public GameBoardView() {
        this.player1Name = "Player 1";
        this.player1Elo = 1000;
        this.player2Name = "Player 2";
        this.player2Elo = 1000;
        this.client = null;
        this.username = "Player 1";
        this.opponent = "Player 2";
        buildUI();
        applyCSS();
        startTimer();
        updateTurnUI();
    }

    public GameBoardView(GameClient client, String username, String opponent) {
        this.client = client;
        this.username = username;
        this.opponent = opponent;

        buildUI();
        applyCSS();
        startTimer();
        addSystemMessage("Match started!");
        updateTurnUI();
    }

    /** Allow pre-loading player info before the scene is shown */
    public GameBoardView(String p1Name, int p1Elo, String p2Name, int p2Elo) {
        this.player1Name = p1Name;
        this.player1Elo  = p1Elo;
        this.player2Name = p2Name;
        this.player2Elo  = p2Elo;
        buildUI();
        applyCSS();
        startTimer();
        addSystemMessage("Match started! " + player1Name + " goes first.");
        updateTurnUI();
    }

    public Scene getScene() { return scene; }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI CONSTRUCTION
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20;");

        root.setLeft(buildPlayerCard(1));
        root.setCenter(buildCenterColumn());
        root.setRight(buildRightColumn());

        BorderPane.setMargin(root.getLeft(),   new Insets(0, 14, 0, 0));
        BorderPane.setMargin(root.getRight(),  new Insets(0, 0, 0, 14));

        scene = new Scene(root, 1080, 720);
    }

    // ── Player card ───────────────────────────────────────────────────────────
    private VBox buildPlayerCard(int player) {
        VBox card = new VBox(12);
        card.setPrefWidth(190);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
            "-fx-background-color: #1e2535;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 18 14 18 14;" +
            "-fx-border-color: #252a3d;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );

        String name   = player == 1 ? player1Name : player2Name;
        int    elo    = player == 1 ? player1Elo  : player2Elo;
        String color  = player == 1 ? "#ff3b3b"   : "#f5c400";
        String initials = name.length() >= 2
            ? name.substring(0, 2).toUpperCase() : name.toUpperCase();

        // Accent stripe
        Region stripe = new Region();
        stripe.setPrefHeight(3);
        stripe.setMaxWidth(Double.MAX_VALUE);
        stripe.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        // Avatar
        StackPane avatar = new StackPane();
        Circle avatarBg = new Circle(26);
        avatarBg.setFill(Color.web(color + "22"));
        avatarBg.setStroke(Color.web(color));
        avatarBg.setStrokeWidth(2);
        Label avatarText = new Label(initials);
        avatarText.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        avatar.getChildren().addAll(avatarBg, avatarText);

        // Name & ELO
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #e8eaf0; -fx-font-size: 14px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label eloLabel = new Label("ELO  " + elo);
        eloLabel.setStyle("-fx-text-fill: #5a607a; -fx-font-size: 11px;");

        // Piece preview circle (bounces when it's your turn)
        Circle piecePreview = new Circle(14);
        piecePreview.setFill(Color.web(color));
        piecePreview.setEffect(new javafx.scene.effect.Glow(0.6));
        piecePreview.setOpacity(0.25);
        if (player == 1) {
            // store ref so we can animate later
        }

        // Turn indicator dot
        Label turnDot = new Label("● YOUR TURN");
        turnDot.setStyle("-fx-text-fill: #3b6fff; -fx-font-size: 10px;");
        turnDot.setVisible(false);
        if (player == 1) { p1TurnDot = turnDot; p1Card = card; }
        else              { p2TurnDot = turnDot; p2Card = card; }

        // Stats row
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER);
        stats.setStyle(
            "-fx-padding: 10 0 0 0;" +
            "-fx-border-color: #252a3d;" +
            "-fx-border-width: 1 0 0 0;"
        );

        // Store piecePreview ref for turn animation
        if (player == 1) {
            piecePreview.setId("p1preview");
        } else {
            piecePreview.setId("p2preview");
        }

        card.getChildren().addAll(stripe, avatar, nameLabel, eloLabel, piecePreview, turnDot, stats);

        // Store card ref
        if (player == 1) p1Card = card;
        else             p2Card = card;

        return card;
    }

    private Label statBox(String val, String label) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label v = new Label(val);
        v.setStyle("-fx-text-fill: #e8eaf0; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #5a607a; -fx-font-size: 10px;");
        box.getChildren().addAll(v, l);
        // return a placeholder; we use this inline
        return v;
    }

    // ── Center column (timer + board + status) ────────────────────────────────
    private VBox buildCenterColumn() {
        VBox col = new VBox(14);
        col.setAlignment(Pos.TOP_CENTER);

        // Timer
        col.getChildren().add(buildTimerSection());

        // Board
        col.getChildren().add(buildBoard());

        // Status label
        statusLabel = new Label("Waiting for move…");
        statusLabel.setStyle("-fx-text-fill: #5a607a; -fx-font-size: 12px;");
        col.getChildren().add(statusLabel);

        // Post-game buttons (hidden initially)
        postGameButtons = new VBox(8);
        postGameButtons.setAlignment(Pos.CENTER);
        postGameButtons.setVisible(false);

        Button rematchBtn  = new Button("⟳  Rematch");
        Button newGameBtn  = new Button("New Game");
        Button lobbyBtn    = new Button("Back to Lobby");

        styleBtn(rematchBtn, "#3b6fff", "white");
        styleBtn(newGameBtn, "#1e2535", "#e8eaf0");
        styleBtn(lobbyBtn,   "#2a1414", "#ff3b3b");

        rematchBtn.setOnAction(e -> resetGame());
        newGameBtn.setOnAction(e -> resetGame());
        lobbyBtn.setOnAction(e  -> {
            if (timer != null) timer.stop();
            ViewManager.switchTo("Menu");
        });

        HBox btnRow = new HBox(10, rematchBtn, newGameBtn, lobbyBtn);
        btnRow.setAlignment(Pos.CENTER);
        postGameButtons.getChildren().add(btnRow);
        col.getChildren().add(postGameButtons);

        return col;
    }

    private VBox buildTimerSection() {
        VBox wrap = new VBox(4);
        wrap.setAlignment(Pos.CENTER);

        Label timerHeading = new Label("TURN TIMER");
        timerHeading.setStyle("-fx-text-fill: #5a607a; -fx-font-size: 10px;");

        timerLabel = new Label("30");
        timerLabel.setStyle(
            "-fx-text-fill: #e8eaf0; -fx-font-size: 40px; -fx-font-weight: bold;"
        );

        timerBar = new ProgressBar(1.0);
        timerBar.setPrefWidth(200);
        timerBar.setPrefHeight(6);
        timerBar.setStyle("-fx-accent: #3b6fff;");

        wrap.getChildren().addAll(timerHeading, timerLabel, timerBar);
        return wrap;
    }

    private VBox buildBoard() {
        VBox boardWrap = new VBox();
        boardWrap.setAlignment(Pos.CENTER);
        boardWrap.setStyle(
            "-fx-background-color: #12151f;" +
            "-fx-background-radius: 14;" +
            "-fx-padding: 16;" +
            "-fx-border-color: #252a3d;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );

        boardGrid = new GridPane();
        boardGrid.setHgap(CELL_GAP);
        boardGrid.setVgap(CELL_GAP);
        boardGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Circle circle = new Circle(CELL_RADIUS);
                circle.setFill(COLOR_EMPTY);
                circle.setStroke(Color.web("#222844"));
                circle.setStrokeWidth(2);

                final int c = col;
                circle.setOnMouseClicked(e -> handleClick(c));
                circle.setOnMouseEntered(e -> {
                    if (!gameOver && board[0][c] == 0)
                        circle.setStroke(currentPlayer == 1 ? COLOR_P1 : COLOR_P2);
                });
                circle.setOnMouseExited(e ->
                    circle.setStroke(Color.web("#222844"))
                );
                circle.setStyle("-fx-cursor: hand;");

                cells[row][col] = circle;
                boardGrid.add(circle, col, row);
            }
        }

        boardWrap.getChildren().add(boardGrid);
        return boardWrap;
    }

    // ── Right column (player 2 card + chat) ──────────────────────────────────
    private VBox buildRightColumn() {
        VBox col = new VBox(14);
        col.setAlignment(Pos.TOP_CENTER);
        col.setPrefWidth(210);

        col.getChildren().add(buildPlayerCard(2));
        col.getChildren().add(buildChatPanel());

        return col;
    }

    private VBox buildChatPanel() {
        VBox panel = new VBox(0);
        panel.setStyle(
            "-fx-background-color: #1e2535;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252a3d;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        panel.setPrefHeight(280);

        // Header
        Label header = new Label("● IN-GAME CHAT");
        header.setStyle(
            "-fx-text-fill: #5a607a; -fx-font-size: 10px; -fx-padding: 12 14 10 14;" +
            "-fx-border-color: #252a3d; -fx-border-width: 0 0 1 0;"
        );
        header.setMaxWidth(Double.MAX_VALUE);

        // Messages
        chatMessages = new VBox(8);
        chatMessages.setStyle("-fx-padding: 10 12 8 12;");
        chatScroll = new ScrollPane(chatMessages);
        chatScroll.setFitToWidth(true);
        chatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(chatScroll, javafx.scene.layout.Priority.ALWAYS);

        // Input row
        chatInput = new TextField();
        chatInput.setPromptText("Message…");
        chatInput.setStyle(
            "-fx-background-color: #12151f; -fx-text-fill: #e8eaf0;" +
            "-fx-prompt-text-fill: #3a3f58; -fx-background-radius: 6;" +
            "-fx-border-color: #252a3d; -fx-border-radius: 6; -fx-font-size: 12px;"
        );
        chatInput.setOnAction(e -> sendChat());
        HBox.setHgrow(chatInput, javafx.scene.layout.Priority.ALWAYS);

        chatSend = new Button("↑");
        chatSend.setStyle(
            "-fx-background-color: #3b6fff; -fx-text-fill: white;" +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        chatSend.setOnAction(e -> sendChat());

        HBox inputRow = new HBox(6, chatInput, chatSend);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setStyle(
            "-fx-padding: 8 12 10 12;" +
            "-fx-border-color: #252a3d; -fx-border-width: 1 0 0 0;"
        );

        panel.getChildren().addAll(header, chatScroll, inputRow);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GAME LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    private void handleClick(int col) {
        if (gameOver) return;

        try {
            String msg = "MOVE;" + username + ";" + opponent + ";col=" + col;
            client.sendMessage(msg);
            System.out.println("Sent move: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void applyMove(String message) {
        String[] parts = message.split(";");
        String payload = parts[3]; // "col=3"

        int col = Integer.parseInt(payload.split("=")[1]);

        int row = dropRow(col);
        if (row == -1) return;

        board[row][col] = currentPlayer;
        animateDrop(row, col);

        int[][] winCells = checkWinner(row, col);
        if (winCells != null) {
            endGame("win", winCells);
        } else if (checkFull()) {
            endGame("draw", null);
        } else {
            switchPlayer();
            resetTimer();
        }
    }

    private int dropRow(int col) {
        for (int r = ROWS - 1; r >= 0; r--)
            if (board[r][col] == 0) return r;
        return -1;
    }

    private void animateDrop(int row, int col) {
        Circle cell = cells[row][col];
        Color target = currentPlayer == 1 ? COLOR_P1 : COLOR_P2;
        cell.setFill(target);

        // Scale pop animation
        ScaleTransition pop = new ScaleTransition(Duration.millis(220), cell);
        pop.setFromX(0.3); pop.setFromY(0.3);
        pop.setToX(1.0);   pop.setToY(1.0);
        pop.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1.0));
        pop.play();

        // Glow effect
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.5);
        cell.setEffect(glow);
    }

    private int[][] checkWinner(int row, int col) {
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};
        int p = board[row][col];
        for (int[] d : dirs) {
            java.util.List<int[]> line = new java.util.ArrayList<>();
            line.add(new int[]{row, col});
            for (int sign : new int[]{1,-1}) {
                int r = row + d[0]*sign, c = col + d[1]*sign;
                while (r>=0 && r<ROWS && c>=0 && c<COLS && board[r][c]==p) {
                    line.add(new int[]{r,c}); r+=d[0]*sign; c+=d[1]*sign;
                }
            }
            if (line.size() >= 4) return line.toArray(new int[0][]);
        }
        return null;
    }

    private boolean checkFull() {
        for (int c = 0; c < COLS; c++) if (board[0][c] == 0) return false;
        return true;
    }

    private void switchPlayer() {
        currentPlayer = currentPlayer == 1 ? 2 : 1;
        updateTurnUI();
    }

    private void updateTurnUI() {
        String name = currentPlayer == 1 ? player1Name : player2Name;
        statusLabel.setText(name + "'s turn");

        p1TurnDot.setVisible(currentPlayer == 1);
        p2TurnDot.setVisible(currentPlayer == 2);

        String activeStyle  =
            "-fx-background-color: #1e2535; -fx-background-radius: 12;" +
            "-fx-padding: 18 14 18 14; -fx-border-radius: 12; -fx-border-width: 2;" +
            "-fx-border-color: #3b6fff44;";
        String inactiveStyle =
            "-fx-background-color: #1e2535; -fx-background-radius: 12;" +
            "-fx-padding: 18 14 18 14; -fx-border-radius: 12; -fx-border-width: 1;" +
            "-fx-border-color: #252a3d;";

        p1Card.setStyle(currentPlayer == 1 ? activeStyle : inactiveStyle);
        p2Card.setStyle(currentPlayer == 2 ? activeStyle : inactiveStyle);
    }

    private void endGame(String result, int[][] winCells) {
        gameOver = true;
        if (timer != null) timer.stop();
        chatInput.setDisable(true);
        chatSend.setDisable(true);

        if (result.equals("win") && winCells != null) {
            flashWinCells(winCells);
            String winner = currentPlayer == 1 ? player1Name : player2Name;
            statusLabel.setStyle("-fx-text-fill: #e8eaf0; -fx-font-size: 14px; -fx-font-weight: bold;");
            statusLabel.setText("🏆  " + winner + " wins!");
            addSystemMessage(winner + " wins! 🏆");
        } else {
            statusLabel.setText("It's a draw! 🤝");
            addSystemMessage("It's a draw! 🤝");
        }

        postGameButtons.setVisible(true);
    }

    private void flashWinCells(int[][] winCells) {
        for (int[] pos : winCells) {
            Circle cell = cells[pos[0]][pos[1]];
            Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(cell.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(350), new KeyValue(cell.opacityProperty(), 0.35)),
                new KeyFrame(Duration.millis(700), new KeyValue(cell.opacityProperty(), 1.0))
            );
            flash.setCycleCount(Animation.INDEFINITE);
            flash.play();
        }
    }

    private void resetGame() {
        board         = new int[ROWS][COLS];
        currentPlayer = 1;
        gameOver      = false;

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                cells[r][c].setFill(COLOR_EMPTY);
                cells[r][c].setEffect(null);
                cells[r][c].setOpacity(1.0);
            }

        chatInput.setDisable(false);
        chatSend.setDisable(false);
        postGameButtons.setVisible(false);
        statusLabel.setStyle("-fx-text-fill: #5a607a; -fx-font-size: 12px;");

        resetTimer();
        updateTurnUI();
        addSystemMessage("New game started! " + player1Name + " goes first.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TIMER
    // ─────────────────────────────────────────────────────────────────────────

    private void startTimer() {
        timeLeft = TURN_SECS;
        updateTimerUI();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            updateTimerUI();
            if (timeLeft <= 0) {
                timer.stop();
                handleTimeout();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    private void resetTimer() {
        if (timer != null) timer.stop();
        startTimer();
    }

    private void updateTimerUI() {
        timerLabel.setText(String.valueOf(timeLeft));
        timerBar.setProgress((double) timeLeft / TURN_SECS);

        if (timeLeft <= 5) {
            timerLabel.setStyle("-fx-text-fill: #ff3b3b; -fx-font-size: 40px; -fx-font-weight: bold;");
            timerBar.setStyle("-fx-accent: #ff3b3b;");
        } else if (timeLeft <= 10) {
            timerLabel.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 40px; -fx-font-weight: bold;");
            timerBar.setStyle("-fx-accent: #ff6b35;");
        } else {
            timerLabel.setStyle("-fx-text-fill: #e8eaf0; -fx-font-size: 40px; -fx-font-weight: bold;");
            timerBar.setStyle("-fx-accent: #3b6fff;");
        }
    }

    private void handleTimeout() {
        if (gameOver) return;
        String timedOut = currentPlayer == 1 ? player1Name : player2Name;
        addSystemMessage(timedOut + " ran out of time — turn forfeited.");
        switchPlayer();
        resetTimer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CHAT
    // ─────────────────────────────────────────────────────────────────────────

    private void sendChat() {
        String text = chatInput.getText().trim();
        if (text.isEmpty() || gameOver) return;
        addChatMessage(player1Name, 1, text);
        chatInput.clear();
        // TODO: send over WebSocket → server broadcasts to opponent
    }

    private void addChatMessage(String sender, int player, String text) {
        VBox msgBox = new VBox(2);

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String nameColor = player == 1 ? "#ff3b3b" : player == 2 ? "#f5c400" : "#5a607a";

        Label meta = new Label(sender + "  " + time);
        meta.setStyle("-fx-text-fill: " + nameColor + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label body = new Label(text);
        body.setStyle("-fx-text-fill: #e8eaf0; -fx-font-size: 12px;");
        body.setWrapText(true);

        msgBox.getChildren().addAll(meta, body);
        chatMessages.getChildren().add(msgBox);

        // Auto-scroll to bottom
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private void addSystemMessage(String text) {
        Label msg = new Label("● " + text);
        msg.setStyle("-fx-text-fill: #3a3f58; -fx-font-size: 11px; -fx-font-style: italic;");
        msg.setWrapText(true);
        chatMessages.getChildren().add(msg);
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void styleBtn(Button btn, String bg, String fg) {
        btn.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + fg + ";" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 18;"
        );
    }

    private void applyCSS() {
        try {
            java.net.URL cssUrl = getClass().getResource("/style.css");
            if (cssUrl == null) {
                java.io.File cssFile = new java.io.File("Frontend/style.css");
                if (cssFile.exists()) cssUrl = cssFile.toURI().toURL();
            }
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        } catch (Exception e) {
            System.out.println("Could not load style.css");
        }
    }
}
