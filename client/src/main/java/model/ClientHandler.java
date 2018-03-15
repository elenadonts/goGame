package model;

import controller.PlayerWindowController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

/**
 * Handler class for create new thread when user connect
 * on clint part
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class ClientHandler extends Thread {
    private static int serverPort;
    private static String serverIp;
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class);
    private PrintWriter writer;
    private BufferedReader reader;
    private PlayerWindowController guiController;
    public static final String SOCKET_PROPERTIES_PATH = "socket.properties";


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
        uploadSocketProperties();
        try {
            Socket socket = new Socket(serverIp, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            String input;
            while ((input = reader.readLine()) != null) {
                guiController.readXML(input);
            }
        } catch (SocketException e) {
            LOGGER.error("Unable to connect to " + serverIp + ":" + serverPort, e);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Serve offline", ButtonType.OK);
                alert.showAndWait();
                System.exit(0);
            });
        } catch (IOException e) {
            LOGGER.error("Exception initializing writer or reader", e);
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

    /**
     * uploads port and ip values from file
     */
    private void uploadSocketProperties() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream socketProperties = classLoader.getResourceAsStream(SOCKET_PROPERTIES_PATH);
        Properties properties = new Properties();
        try {
            properties.load(socketProperties);
        } catch (IOException e) {
            LOGGER.error("Cannot load socket.properties", e);
        }
        serverIp = properties.getProperty("serverIpAddress");
        serverPort = Integer.parseInt(properties.getProperty("port"));
    }
}