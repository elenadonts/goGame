package model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private BufferedReader reader;
    private PrintWriter writer;

    private Socket clientSocket;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer;

    public ClientHandler(Socket client) {
        this.clientSocket = client;
        try {
            builder = factory.newDocumentBuilder();
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        try {
            System.out.println("User: " + clientSocket.getInetAddress().toString().replace("/", "") + " connected;");
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);//send to client
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//receive from client
        } catch (IOException e) {
            e.printStackTrace();
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

                        System.out.println(stringWriter.toString());
                        writer.println(stringWriter.toString());
                    } catch (SAXException | TransformerException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getMeta(Element element) {
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

    public String testLogin(String login, String password) {
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

    public void createNewUser(String login, String password) {
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
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
