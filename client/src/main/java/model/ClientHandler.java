package model;

import controller.PlayerWindowController;
import org.apache.log4j.Logger;


import java.io.*;
import java.net.Socket;
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
            Socket socket = new Socket(serverIp, serverPort );
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

    /**
     * uploads port and ip values from file
     */
    private static void uploadSocketProperties() {
        ClassLoader classLoader = ClientHandler.class.getClassLoader();
        File socketProperties = new File(classLoader.getResource(SOCKET_PROPERTIES_PATH).getFile());
        Properties properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(socketProperties);
            properties.load(inputStream);
        }
        catch (IOException e){
            LOGGER.error("Cannot load socket.properties", e);
        }
        serverIp = properties.getProperty("serverIpAddress");
        serverPort = Integer.parseInt(properties.getProperty("port"));
    }
}
