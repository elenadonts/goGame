package controller;

import java.net.ServerSocket;

public class Server {
    public static final int PORT = 3000;
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        try {
            System.out.println("Listening...");
            while (true) {
                new ClientHandler(server.accept());
            }
        } finally {
            server.close();
        }
    }
}
