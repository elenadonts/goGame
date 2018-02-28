package controller;

import model.GameRoom;
import model.Player;
import org.apache.log4j.Logger;
import model.ClientHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class);
    public static final int PORT = 3000;
    public static DocumentBuilder docBuilder;
    public static HashMap<String, Player> userList = new HashMap<>();
    public static HashSet<PrintWriter> writers = new HashSet<>();
    public static HashSet<Player> userOnline = new HashSet<>();
    public static HashMap<String,GameRoom> gameRooms = new HashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        uploadUserList();
        try {
            System.out.println("Listening...");
            while (true) {
                ClientHandler clientHandler = new ClientHandler(server.accept()) ;
                clientHandler.start();
            }
        } finally {
            server.close();
        }
    }

    private static void uploadUserList() {
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
                userList.put(player.getUserName(), player);
            } catch (SAXException | IOException e) {
                logger.error("Exception", e);
            }
        }
    }
}
