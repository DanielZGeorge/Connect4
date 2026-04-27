package model;

import java.nio.channels.SocketChannel;

public class HumanPlayer extends Player {
    private SocketChannel socketChannel; // wired by NioServer (George)
    private final long queueJoinTime;
    private int currentEloRange;

    public HumanPlayer(String username, int elo, SocketChannel socketChannel) {
        super(username, elo);
        this.socketChannel = socketChannel;
        this.queueJoinTime = System.currentTimeMillis();
        this.currentEloRange = 100;
    }

    @Override
    public String getName() { return username; }

    public SocketChannel getSocketChannel() { return socketChannel; }
    public void setSocketChannel(SocketChannel socketChannel) { this.socketChannel = socketChannel; }
    public long getQueueJoinTime() { return queueJoinTime; }
    public int getCurrentEloRange() { return currentEloRange; }
    public void setCurrentEloRange(int range) { this.currentEloRange = range; }
}
