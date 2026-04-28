package server;

public class NioServerMain {
    public static void main(String[] args) {
    	
    	System.out.println("Hello");
        int port = 9090;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, defaulting to 9090.");
            }
        }
        NioServer server = new NioServer(port);
        server.startServer();
    }
}
