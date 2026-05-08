package server;

public class NetworkMessage {
    private String type;
    private String sender;
    private String target;
    private String payload;
    
    public NetworkMessage(String type, String sender, String target, String payload) {
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.payload = payload;
    }
    
    public static NetworkMessage deserialize(String data) {
        String[] parts = data.split(";", 4);
        if (parts.length < 4) {
            return null;
        }
        return new NetworkMessage(parts[0], parts[1], parts[2], parts[3]);
    }
    
    public String serialize() {
        return type + ";" + sender + ";" + target + ";" + payload;
    }
    
    public String getType() { return type; }
    public String getSender() { return sender; }
    public String getTarget() { return target; }
    public String getPayload() { return payload; }
}