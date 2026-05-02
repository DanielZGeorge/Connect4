package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NioTestServer {
    public static void main(String[] args) {
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9090));
            System.out.println("Connected to server...");

            // Background thread to read messages from the server
            Thread readThread = new Thread(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                try {
                    while (channel.read(buffer) != -1) {
                        buffer.flip();
                        String response = StandardCharsets.UTF_8.decode(buffer).toString();
                        System.out.print("[Server] " + response);
                        buffer.clear();
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            readThread.start();

            // Main thread to read from console and send to server
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                ByteBuffer writeBuffer = StandardCharsets.UTF_8.encode(input + "\n");
                channel.write(writeBuffer);
            }

            channel.close();
            scanner.close();
        } catch (IOException e) {
            System.err.println("Failed to connect or communicate: " + e.getMessage());
        }
    }
}
