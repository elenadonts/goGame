package server.controller;

import org.apache.log4j.Logger;
import server.model.ClientHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class);
    public static final int PORT = 3000;
    public static DocumentBuilder docBuilder;
    public static HashMap<String, String> userList = new HashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        uploadUserList();
        try {
            System.out.println("Listening...");
            while (true) {
                new ClientHandler(server.accept());
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
                userList.put(userElement.getElementsByTagName("login").item(0).getTextContent()
                        , userElement.getElementsByTagName("password").item(0).getTextContent());
            } catch (SAXException | IOException e) {
                logger.error("Excepion", e);
            }
        }
    }

}
