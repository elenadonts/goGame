package model;

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
    public static final int PORT = 3000;
    private static DocumentBuilder docBuilder;
    static HashMap<String, String> userList = new HashMap<>();

    public static void main(String[] args) throws Exception {
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Listening...");
            uploadUserList();
            while (true) {
                new ClientHandler(server.accept());
            }
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
                e.printStackTrace();
            }
        }
    }
}
