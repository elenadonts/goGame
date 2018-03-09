package model;

import controller.PlayerWindowController;
import org.apache.log4j.Logger;


import java.io.*;
import java.net.Socket;

/**
 * Handler class for create new thread when user connect
 * on clint part
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class ClientHandler extends Thread {
    private static final int SERVER_PORT = 3000;
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class);
    private PrintWriter writer;
    private PlayerWindowController guiController;

    /**
     * Set object playerWindowController class for this class
     *
     * @param guiController the object
     */
    public void setGuiController(PlayerWindowController guiController) {
        this.guiController = guiController;
    }


    /**
     * reads info from server and send this info to
     * player window controller
     */
    public void run() {
        try {
            Socket socket = new Socket("localhost", SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            String input;
            while ((input = reader.readLine()) != null) {
                guiController.readXML(input);
            }
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }
    }

    /**
     * Sends message to server from client
     *
     * @param message for sends
     */
    public void send(String message) {
        writer.println(message);
    }

}
