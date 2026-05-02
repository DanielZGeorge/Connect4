package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class GameClient {
    private SocketChannel channel;

    public void connect() throws IOException {
        channel = SocketChannel.open(new InetSocketAddress("localhost", 9090));
        System.out.println("Connected to server");
    }

    public void sendMessage(String message) throws IOException {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(message + "\n");
        channel.write(buffer);
    }

    public void disconnect() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }
    
    public void listen() {
        new Thread(() -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                while (true) {
                    buffer.clear();
                    int bytesRead = channel.read(buffer);

                    if (bytesRead > 0) {
                        buffer.flip();
                        String response = StandardCharsets.UTF_8.decode(buffer).toString();
                        if (response.contains("MATCH_QUEUED")) {
                            System.out.println("You are in queue...");
                        }
                        System.out.println("Server says: " + response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}