package server;

import dao.UserDAO;
import jakarta.websocket.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameHub {
    private static final GameHub INSTANCE = new GameHub();
    private static final long TURN_MILLIS = 30_000L;

    private final Matchmaker matchmaker;
    private final Thread matchmakerThread;
    private final ScheduledExecutorService scheduler;

    private final ConcurrentMap<String, ClientInfo> clients = new ConcurrentHashMap<>();
    private final ConcurrentMap<Session, String> sessionUsers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, GameSession> gamesById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, GameSession> userToGame = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<Session>> spectatorsByGame = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> spectatorToGame = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, botPlayer> botByGameId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> botNameByGameId = new ConcurrentHashMap<>();

    private GameHub() {
        this.matchmaker = new Matchmaker(this);
        this.matchmakerThread = new Thread(matchmaker, "matchmaker");
        this.matchmakerThread.setDaemon(true);
        this.matchmakerThread.start();

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::checkTimeouts, 1, 1, TimeUnit.SECONDS);
    }

    public static GameHub getInstance() {
        return INSTANCE;
    }

    public void registerClient(String username, Session session, boolean guest) {
        clients.put(username, new ClientInfo(username, session, guest));
        sessionUsers.put(session, username);
    }

    public void ensureClient(String username, Session session) {
        if (username == null || session == null) return;
        if (!clients.containsKey(username)) {
            registerClient(username, session, false);
        }
    }

    public void unregister(Session session) {
        String username = sessionUsers.remove(session);
        if (username == null) return;

        clients.remove(username);
        matchmaker.removePlayer(username);

        String spectatedGame = spectatorToGame.remove(username);
        if (spectatedGame != null) {
            Set<Session> spectators = spectatorsByGame.get(spectatedGame);
            if (spectators != null) spectators.remove(session);
        }

        GameSession game = userToGame.remove(username);
        if (game != null) {
            game.forfeit(username);
            String opponent = game.getOpponent(username);
            if (opponent != null) {
                userToGame.remove(opponent);
            }
            broadcastState(game);
            finalizeGame(game);
        }
    }

    public void requestMatch(String username) {
        ClientInfo info = clients.get(username);
        if (info == null) return;
        if (info.guest()) {
            sendError(info.session(), "MATCH", "GUEST_NOT_ALLOWED");
            return;
        }
        if (userToGame.containsKey(username)) {
            sendError(info.session(), "MATCH", "ALREADY_IN_MATCH");
            return;
        }

        int elo = resolveElo(username);
        if (elo < 0) {
            sendError(info.session(), "MATCH", "UNKNOWN_USER");
            return;
        }

        matchmaker.addPlayer(new Matchmaker.WaitingPlayer(username, elo, System.currentTimeMillis()));
        sendAck(info.session(), "MATCH_QUEUED");
    }

    public void requestBotMatch(String username, String payload) {
        ClientInfo info = clients.get(username);
        if (info == null) return;
        if (info.guest()) {
            sendError(info.session(), "BOT", "GUEST_NOT_ALLOWED");
            return;
        }
        if (userToGame.containsKey(username)) {
            sendError(info.session(), "BOT", "ALREADY_IN_MATCH");
            return;
        }

        String difficulty = parseDifficulty(payload);
        createBotMatch(username, difficulty);
    }

    public void requestSpectate(String username, String targetPlayer) {
        ClientInfo spectator = clients.get(username);
        if (spectator == null) return;

        GameSession game = userToGame.get(targetPlayer);
        if (game == null) {
            sendError(spectator.session(), "SPECTATE", "TARGET_NOT_IN_MATCH");
            return;
        }

        spectatorToGame.put(username, game.getGameId());
        spectatorsByGame.computeIfAbsent(game.getGameId(), key -> ConcurrentHashMap.newKeySet()).add(spectator.session());
        sendState(spectator.session(), game);
    }

    public void handleMove(String username, int col) {
        GameSession game = userToGame.get(username);
        if (game == null) {
            sendErrorForUser(username, "MOVE", "NOT_IN_MATCH");
            return;
        }

        GameSession.MoveResult result = game.applyMove(username, col);
        if (!result.accepted()) {
            sendErrorForUser(username, "MOVE", result.reason());
            return;
        }

        broadcastState(game);
        maybePlayBot(game);
        if (game.isGameOver()) {
            finalizeGame(game);
        }
    }

    public void handleChat(String username, String text) {
        ClientInfo sender = clients.get(username);
        if (sender == null) return;

        GameSession game = userToGame.get(username);
        if (game == null) {
            sendError(sender.session(), "CHAT", "NOT_IN_MATCH");
            return;
        }

        if (game.isGameOver()) {
            sendError(sender.session(), "CHAT", "GAME_OVER");
            return;
        }

        if (spectatorToGame.containsKey(username)) {
            sendError(sender.session(), "CHAT", "SPECTATORS_CANNOT_CHAT");
            return;
        }

        String opponent = game.getOpponent(username);
        if (opponent == null) return;
        ClientInfo opponentInfo = clients.get(opponent);
        if (opponentInfo == null) return;

        NetworkMessage msg = new NetworkMessage("CHAT", username, opponent, text);
        sendMessage(opponentInfo.session(), msg);
        sendAck(sender.session(), "CHAT_SENT");
    }

    public void createMatch(String user1, String user2) {
        ClientInfo p1 = clients.get(user1);
        ClientInfo p2 = clients.get(user2);
        if (p1 == null || p2 == null) return;

        GameSession game = new GameSession(user1, user2, true);
        game.resetTurnDeadline(TURN_MILLIS);
        gamesById.put(game.getGameId(), game);
        userToGame.put(user1, game);
        userToGame.put(user2, game);

        sendState(p1.session(), game);
        sendState(p2.session(), game);
    }

    private void createBotMatch(String username, String difficulty) {
        ClientInfo player = clients.get(username);
        if (player == null) return;

        String botName = buildBotName(difficulty);
        botPlayer bot = buildBot(difficulty, botName);
        if (bot == null) {
            sendError(player.session(), "BOT", "INVALID_DIFFICULTY");
            return;
        }

        GameSession game = new GameSession(username, botName, false);
        game.resetTurnDeadline(TURN_MILLIS);
        gamesById.put(game.getGameId(), game);
        userToGame.put(username, game);
        userToGame.put(botName, game);
        botByGameId.put(game.getGameId(), bot);
        botNameByGameId.put(game.getGameId(), botName);

        sendState(player.session(), game);
        maybePlayBot(game);
    }

    private void maybePlayBot(GameSession game) {
        if (game.isGameOver()) return;
        botPlayer bot = botByGameId.get(game.getGameId());
        if (bot == null) return;

        String botName = botNameByGameId.get(game.getGameId());
        if (botName == null) return;

        if (game.getPlayerNumber(botName) != game.getCurrentUser()) return;

        int move = bot.chooseMove(game);
        if (move < 0) {
            game.forfeit(botName);
            broadcastState(game);
            finalizeGame(game);
            return;
        }

        GameSession.MoveResult result = game.applyMove(botName, move);
        if (result.accepted()) {
            broadcastState(game);
            if (game.isGameOver()) {
                finalizeGame(game);
            }
        }
    }

    private void broadcastState(GameSession game) {
        ClientInfo p1 = clients.get(game.getUser1());
        ClientInfo p2 = clients.get(game.getUser2());
        if (p1 != null) sendState(p1.session(), game);
        if (p2 != null) sendState(p2.session(), game);

        Set<Session> spectators = spectatorsByGame.getOrDefault(game.getGameId(), new HashSet<>());
        for (Session session : spectators) {
            sendState(session, game);
        }
    }

    private void finalizeGame(GameSession game) {
        game.endGame(game.getWinner(), game.isRanked());
        gamesById.remove(game.getGameId());
        botByGameId.remove(game.getGameId());
        botNameByGameId.remove(game.getGameId());
        userToGame.remove(game.getUser1());
        userToGame.remove(game.getUser2());
        spectatorsByGame.remove(game.getGameId());
        spectatorToGame.entrySet().removeIf(entry -> entry.getValue().equals(game.getGameId()));
    }

    public List<ActiveMatch> getActiveMatchesSnapshot() {
        List<ActiveMatch> matches = new ArrayList<>();
        for (GameSession game : gamesById.values()) {
            matches.add(new ActiveMatch(game.getGameId(), game.getUser1(), game.getUser2()));
        }
        return matches;
    }

    public boolean isPlayerInMatch(String username) {
        return userToGame.containsKey(username);
    }

    private int resolveElo(String username) {
        try {
            UserDAO userDAO = new UserDAO();
            model.User user = userDAO.getUserByUsername(username);
            return user == null ? -1 : user.getEloRating();
        } catch (Exception e) {
            return -1;
        }
    }

    private void sendState(Session session, GameSession game) {
        NetworkMessage msg = new NetworkMessage("STATE", "server", game.getGameId(), game.buildSnapshotJson());
        sendMessage(session, msg);
    }

    private void sendAck(Session session, String payload) {
        NetworkMessage msg = new NetworkMessage("ACK", "server", "client", payload);
        sendMessage(session, msg);
    }

    private void sendError(Session session, String type, String error) {
        NetworkMessage msg = new NetworkMessage("ERROR", "server", type, error);
        sendMessage(session, msg);
    }

    private void sendErrorForUser(String username, String type, String error) {
        ClientInfo info = clients.get(username);
        if (info == null) return;
        sendError(info.session(), type, error);
    }

    private void sendMessage(Session session, NetworkMessage message) {
        if (session == null || !session.isOpen()) return;
        try {
            session.getBasicRemote().sendText(message.serialize());
        } catch (IOException e) {
            // Ignore failed delivery.
        }
    }

    private void checkTimeouts() {
        long now = System.currentTimeMillis();
        for (GameSession game : gamesById.values()) {
            if (game.handleTimeout(now)) {
                broadcastState(game);
                maybePlayBot(game);
                if (game.isGameOver()) {
                    finalizeGame(game);
                }
            }
        }
    }

    private String parseDifficulty(String payload) {
        if (payload == null || payload.isBlank()) return "easy";
        String[] parts = payload.split("=", 2);
        if (parts.length == 2 && "difficulty".equalsIgnoreCase(parts[0])) {
            return parts[1].trim().toLowerCase();
        }
        return payload.trim().toLowerCase();
    }

    private String buildBotName(String difficulty) {
        return "BOT_" + difficulty.toUpperCase();
    }

    private botPlayer buildBot(String difficulty, String botName) {
        switch (difficulty) {
            case "easy":
                return new easyBot(-1, botName, 0);
            case "medium":
                return new mediumBot(-1, botName, 0);
            case "hard":
                return new hardBot(-1, botName, 0);
            default:
                return null;
        }
    }

    public record ClientInfo(String username, Session session, boolean guest) {
    }

    public record ActiveMatch(String gameId, String player1, String player2) {
    }
}




