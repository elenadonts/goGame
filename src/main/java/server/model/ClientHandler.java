package server.model;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import server.controller.Server;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread{
    private static final Logger logger = Logger.getLogger(ClientHandler.class);
    private BufferedReader reader;
    private PrintWriter writer;

    private Socket clientSocket;

    private DocumentBuilder builder;
    private Transformer transformer;

    public ClientHandler(Socket client) {
        this.clientSocket = client;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            TransformerFactory tf = TransformerFactory.newInstance();
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            logger.error("Exception", e);
        }
        start();
    }

    @Override
    public void run() {
        try {
            System.out.println("User: " + clientSocket.getInetAddress().toString().replace("/", "") + " connected;");
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);//send to java.client
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//receive from java.client
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        while (true) {
            String input;
            String output;
            try {
                if (reader.ready()) {
                    input = reader.readLine();
                    try {
                        Document document = builder.parse(new InputSource(new StringReader(input)));
                        Node user = document.getElementsByTagName("body").item(0);
                        output = getMeta((Element) user);
                        document = builder.newDocument();

                        Element root = document.createElement("body");
                        document.appendChild(root);

                        Element meta = document.createElement("meta-info");
                        meta.appendChild(document.createTextNode(output));
                        root.appendChild(meta);

                        StringWriter stringWriter = new StringWriter();
                        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

                        writer.println(stringWriter.toString());
                    } catch (SAXException | TransformerException e) {
                        logger.error("Exception", e);
                    }
                }
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
    }

    private String getMeta(Element element) {
        String meta = element.getElementsByTagName("meta-info").item(0).getTextContent();
        String command;
        switch (meta) {
            case "login":
                command = testLogin(element.getElementsByTagName("login").item(0).getTextContent(),
                        element.getElementsByTagName("password").item(0).getTextContent());
                break;
            default:
                command = "";
                break;
        }
        return command;
    }

    private String testLogin(String login, String password) {
        String info = "connect";
        if (!Server.userList.containsKey(login)) {
            createNewUser(login, password);
        } else {
            if (!Server.userList.get(login).equals(password)) {
                info = "incorrect";
            }
        }
        return info;
    }

    private void createNewUser(String login, String password) {
        Server.userList.put(login,password);
        File file = new File("users" + File.separator + login + ".xml");
        Document doc = builder.newDocument();

        Element root = doc.createElement("body");
        doc.appendChild(root);

        Element log = doc.createElement("login");
        log.appendChild(doc.createTextNode(login));
        root.appendChild(log);

        Element pass = doc.createElement("password");
        pass.appendChild(doc.createTextNode(password));
        root.appendChild(pass);
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (TransformerException e) {
            logger.error("TransformerException", e);
        }
    }
}
