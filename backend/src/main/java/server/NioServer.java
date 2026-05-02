package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// other .java files in this folder are just placeholders so that I could use them here,
// they need to be replaced with the actual implementations

public class NioServer {
    private static final int BUFFER_SIZE = 4096;

    private final int port;

    Selector selector;
    ServerSocketChannel serverChannel;
    ExecutorService threadpool;
    Map<SocketChannel, player> connectedPlayers;
    List<GameSession> activeGames;

    public NioServer(int port) {
        this.port = port;
        this.threadpool = Executors.newCachedThreadPool();
        this.connectedPlayers = new ConcurrentHashMap<>();
        this.activeGames = new CopyOnWriteArrayList<>();
    }

    public void startServer() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            System.out.println("Successfully booted NIO server on port " + String.valueOf(port));

            while (serverChannel.isOpen()) {
            	
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        acceptConnection();
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        readMessage(client);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start NioServer", e);
        } finally {
            if (threadpool != null) {
                threadpool.shutdown();
            }
        }
    }

    public void acceptConnection() {
        try {
            SocketChannel client = serverChannel.accept();
            if (client == null) {
                return;
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            sendMessage(client, "WELCOME");
        } catch (IOException e) {
            // Connection setup failed; ignore and keep server running.
        }
    }

    public void readMessage(SocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int read;
        try {
            read = client.read(buffer);
            if (read == -1) {
                closeConnection(client);
                return;
            }
        } catch (IOException e) {
            closeConnection(client);
            return;
        }

        buffer.flip();
        String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
        if (message.isEmpty()) {
            return;
        }

        handleRequest(client, message);
    }

    public void sendMessage(SocketChannel client, String message) {
        if (client == null || message == null) {
            return;
        }
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(message + "\n");
        try {
            client.write(buffer);
        } catch (IOException e) {
            closeConnection(client);
        }
    }

    public void handleRequest(SocketChannel client, String rawData) {
        if (rawData == null || rawData.isBlank()) return;

        if ("PING".equalsIgnoreCase(rawData)) {
            sendMessage(client, "PONG");
            return;
        }

        NetworkMessage message = NetworkMessage.deserialize(rawData);
        if (message == null) {
            sendMessage(client, "ERROR;Invalid message format");
            return;
        }

        player senderPlayer = connectedPlayers.get(client);

        switch (message.getType()) {
            case "MATCH":
                // Assuming payload contains player details or joining intent
                // humanPlayer newPlayer = new humanPlayer(...)
                // connectedPlayers.put(client, newPlayer);
                // Matchmaker.addPlayer(newPlayer);
                sendMessage(client, "ACK;MATCH_QUEUED");
                break;
            case "CHAT":
            case "MOVE":
                // Find opponent socket channel to route the message
                SocketChannel targetChannel = findChannelByUsername(message.getTarget());
                if (targetChannel != null) {
                    sendMessage(targetChannel, message.serialize());
                    sendMessage(client, "ACK;" + message.getType() + "_SENT");
                } else {
                    sendMessage(client, "ERROR;Target not found");
                }
                break;
            default:
                sendMessage(client, "ERROR;Unknown command");
                break;
        }
    }

    private SocketChannel findChannelByUsername(String username) {
        for (Map.Entry<SocketChannel, player> entry : connectedPlayers.entrySet()) {
            if (entry.getValue() != null && username.equals(entry.getValue().getUsername())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void closeConnection(SocketChannel client) {
        connectedPlayers.remove(client);
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (IOException e) {

        }
    }
}