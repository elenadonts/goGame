package controller;

import model.ClientHandler;
import model.GameRoom;
import model.Player;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Main class for start server
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private static final int PORT = 3000;
    private static DocumentBuilder docBuilder;
    public static HashMap<String, Player> userList;
    public static HashSet<PrintWriter> writers;
    public static HashMap<String, Player> userOnline;
    public static HashMap<String, GameRoom> gameRooms;
    public static HashSet<String> banList;
    private static final BufferedReader SERVER_CONSOLE = new BufferedReader(new InputStreamReader(System.in));
    private static boolean isRunning;
    private static ServerSocket server;


    public static void main(String[] args) {
        runServer();
    }

    /**
     * Start server
     */
    private static void runServer(){
        isRunning = true;
        writers = new HashSet<>();
        userOnline = new HashMap<>();
        gameRooms = new HashMap<>();
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error(e);
        }
        uploadUserList();
        try {
            server = new ServerSocket(PORT);
            server.setSoTimeout(500);
            LOGGER.info("Server starting...");
            while (isRunning) {
                try {
                    if (SERVER_CONSOLE.ready()){
                        ServerCommand serverCommand = getCommand(SERVER_CONSOLE.readLine());
                        handleCommand(serverCommand);
                    }
                    ClientHandler clientHandler = new ClientHandler(server.accept());
                    clientHandler.start();
                }
                catch (SocketTimeoutException e){
                    continue;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }

    }

    /**
     * Check the input and execute corresponding command
     * @param command command received from console
     */
    private static void handleCommand(ServerCommand command){
        switch (command){
            case STOP: stopServer();
                break;
            case RESTART: restartServer();
                break;
            case UNKNOWN_COMMAND:
                break;
        }
    }

    /**
     * Restart server
     */
    private static void restartServer(){
        LOGGER.info("Restarting server");
        stopServer();
        runServer();
    }

    /**
     * Stop server
     */
    private static void stopServer(){
        LOGGER.info("Stopping server");
        isRunning = false;
        try {
            server.close();
        }
        catch (IOException e){
            LOGGER.error("Exception stopping server");
        }
    }

    /**
     * Parse command to corresponding enum value
     * @param command received input
     * @return corresponding command
     */
    private static ServerCommand getCommand(String command){
        command = command.toLowerCase();
        ServerCommand serverCommand;
        switch (command){
            case "stop": serverCommand = ServerCommand.STOP;
                break;
            case "restart" : serverCommand = ServerCommand.RESTART;
                break;
            default: serverCommand = ServerCommand.UNKNOWN_COMMAND;
                break;
        }
        return serverCommand;
    }

    /**
     * Upload user list from dir 'users'
     */
    private static void uploadUserList() {
        userList = new HashMap<>();
        banList = new HashSet<>();
        File folder = new File("users/");
        folder.mkdir();
        File[] userFileLists = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        for (File userFile : userFileLists) {
            try {
                Document doc = docBuilder.parse(userFile);
                Node user = doc.getElementsByTagName("body").item(0);
                Element userElement = (Element) user;
                Player player = new Player();
                player.setUserName(userElement.getElementsByTagName("login").item(0).getTextContent());
                player.setUserPassword(userElement.getElementsByTagName("password").item(0).getTextContent());
                player.setUserGameCount(userElement.getElementsByTagName("gameCount").item(0).getTextContent());
                player.setUserPercentWins(userElement.getElementsByTagName("percentWins").item(0).getTextContent());
                player.setUserRating(userElement.getElementsByTagName("rating").item(0).getTextContent());
                player.setUserWinGames(userElement.getElementsByTagName("winGames").item(0).getTextContent());
                if (Boolean.parseBoolean(userElement.getElementsByTagName("admin").item(0).getTextContent())) {
                    player.setAdmin(true);
                }
                if (Boolean.parseBoolean(userElement.getElementsByTagName("banned").item(0).getTextContent())) {
                    banList.add(player.getUserName());
                }
                userList.put(player.getUserName(), player);
            } catch (SAXException | IOException e) {
                LOGGER.error("Exception", e);
            }
        }
    }
}
