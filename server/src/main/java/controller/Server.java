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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
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

    public static void main(String[] args) throws Exception {
        writers = new HashSet<>();
        userOnline = new HashMap<>();
        gameRooms = new HashMap<>();
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        uploadUserList();
        try (ServerSocket server = new ServerSocket(PORT)) {
            LOGGER.info("Server starting...");
            while (true) {
                ClientHandler clientHandler = new ClientHandler(server.accept());
                clientHandler.start();
            }
        }
    }

    /**
     * upload user list from dir 'users'
     */
    private static void uploadUserList() {
        userList = new HashMap<>();
        banList = new HashSet<>();
        File folder = new File("users/");
        if (folder.mkdir()) {
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
}
