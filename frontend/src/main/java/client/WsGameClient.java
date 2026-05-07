package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class WsGameClient {
    private static final WsGameClient INSTANCE = new WsGameClient();
    private static final String DEFAULT_WS_URL = "ws://localhost:8080/backend/ws";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final AtomicReference<WebSocket> socketRef = new AtomicReference<>();
    private final AtomicReference<String> currentUser = new AtomicReference<>(null);
    private final AtomicReference<Boolean> currentGuest = new AtomicReference<>(false);
    private final List<String> pendingMessages = new CopyOnWriteArrayList<>();
    private volatile Consumer<String> messageHandler;

    public static WsGameClient getInstance() {
        return INSTANCE;
    }

    public void connectIfNeeded(String username, boolean guest) {
        if (username == null || username.isBlank()) return;
        WebSocket socket = socketRef.get();
        if (socket != null) return;

        currentUser.set(username);
        currentGuest.set(guest);

        CompletableFuture<WebSocket> future = httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(DEFAULT_WS_URL), new Listener());
        future.thenAccept(socketRef::set);
    }

    public void send(String message) {
        WebSocket socket = socketRef.get();
        if (socket == null) {
            pendingMessages.add(message);
            return;
        }
        socket.sendText(message, true);
    }

    public void sendHello() {
        WebSocket socket = socketRef.get();
        if (socket == null) return;
        String username = currentUser.get();
        if (username == null) return;
        boolean guest = currentGuest.get() != null && currentGuest.get();
        String role = guest ? "role=guest" : "role=registered";
        send("HELLO;" + username + ";server;" + role);
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    private class Listener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
            sendHello();
            for (String msg : pendingMessages) {
                webSocket.sendText(msg, true);
            }
            pendingMessages.clear();
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String message = buffer.toString();
                buffer.setLength(0);
                Consumer<String> handler = messageHandler;
                if (handler != null) {
                    handler.accept(message);
                }
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            socketRef.set(null);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            socketRef.set(null);
        }
    }
}
