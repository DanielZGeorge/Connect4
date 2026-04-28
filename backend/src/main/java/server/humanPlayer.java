package server;

import java.nio.channels.SocketChannel;

public class humanPlayer extends player {
    private final SocketChannel channel;

    public humanPlayer(int id, String username, int elo, SocketChannel channel) {
        super(id, username, elo);
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
