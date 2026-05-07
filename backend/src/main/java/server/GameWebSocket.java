package server;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class GameWebSocket {
    private final GameHub hub = GameHub.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        // Wait for HELLO to register the user.
    }

    @OnClose
    public void onClose(Session session) {
        hub.unregister(session);
    }

    @OnMessage
    public void onMessage(Session session, String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return;

        NetworkMessage message = NetworkMessage.deserialize(rawMessage);
        if (message == null) {
            hub.ensureClient("unknown", session);
            return;
        }

        String type = message.getType();
        String sender = message.getSender();
        String target = message.getTarget();
        String payload = message.getPayload();

        if (sender != null && !sender.isBlank()) {
            if ("HELLO".equalsIgnoreCase(type)) {
                boolean guest = payload != null && payload.contains("role=guest");
                hub.registerClient(sender, session, guest);
                return;
            }
            hub.ensureClient(sender, session);
        }

        switch (type.toUpperCase()) {
            case "MATCH":
                hub.requestMatch(sender);
                break;
            case "BOT":
                hub.requestBotMatch(sender, payload);
                break;
            case "SPECTATE":
                hub.requestSpectate(sender, target);
                break;
            case "MOVE":
                Integer col = parseCol(payload);
                if (col != null) {
                    hub.handleMove(sender, col);
                }
                break;
            case "CHAT":
                if (payload != null) {
                    hub.handleChat(sender, payload);
                }
                break;
            default:
                break;
        }
    }

    private Integer parseCol(String payload) {
        if (payload == null) return null;
        String[] parts = payload.split("=", 2);
        if (parts.length != 2) return null;
        if (!"col".equalsIgnoreCase(parts[0])) return null;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

