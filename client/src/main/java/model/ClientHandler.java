package model;

import controller.PlayerWindowController;
import org.apache.log4j.Logger;


import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private static final int SERVER_PORT = 3000;
    private static final Logger logger = Logger.getLogger(ClientHandler.class);
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private PlayerWindowController guiController;

    public void setGuiController(PlayerWindowController guiController) {
        this.guiController = guiController;
    }


    public void run() {
        try {
            socket = new Socket("localhost", SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            String input;
            while ((input = reader.readLine()) != null) {
                guiController.readXML(input);
            }
        } catch (IOException e) {
            logger.error("IOException", e);
        } finally {
            try {
                reader.close();
                writer.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String message) {
        writer.println(message);
    }

}
