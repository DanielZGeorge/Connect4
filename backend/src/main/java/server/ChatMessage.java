package server;

public class ChatMessage {
    private final String sender;
    private final long timestamp;
    private final String text;

    public ChatMessage(String sender, long timestamp, String text) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.text = text;
    }

    public String getSenderUsername() {
        return sender;
    }

    public String getTextMessage() {
        return text;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender + ": " + text;
    }
}
