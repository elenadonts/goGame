package model;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import controller.Server;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class ClientHandler extends Thread {
    private static final Logger logger = Logger.getLogger(ClientHandler.class);
    private BufferedReader reader;
    private PrintWriter writer;

    private Socket clientSocket;

    private DocumentBuilder builder;
    private Transformer transformer;
    private Player currentPlayer;
    private GameRoom currentRoom;

    public ClientHandler(Socket client) {
        this.clientSocket = client;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            TransformerFactory tf = TransformerFactory.newInstance();
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException", e);
        } catch (TransformerConfigurationException e) {
            logger.error("TransformerConfigurationException", e);
        }
        this.setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("User: " + clientSocket.getInetAddress().toString().replace("/", "") + " connected;");
        String input;
        try {
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);//send to java.client
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//receive from java.client
            Server.writers.add(writer);
            while ((input = reader.readLine()) != null) {
                Document document = builder.parse(new InputSource(new StringReader(input)));
                Node user = document.getElementsByTagName("body").item(0);

                document = builder.newDocument();
                document = createXML((Element) user, document);

                StringWriter stringWriter = new StringWriter();
                transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

                if (!document.getElementsByTagName("meta-info").item(0).getTextContent().equals("")) {
                    writer.println(stringWriter.toString());

                }
                if (document.getElementsByTagName("meta-info").item(0).getTextContent().equals("connect")) {
                    for (PrintWriter writer : Server.writers) {
                        for (Map.Entry<String, Player> entry : Server.userOnline.entrySet()) {
                            if (!writer.equals(this.writer)) {
                                writer.println(createXMLForUserList("online", entry.getValue()));
                            } else if (!entry.getValue().getUserName().equals(currentPlayer.getUserName())) {
                                writer.println(createXMLForUserList("online", entry.getValue()));
                            }
                        }
                        for (Map.Entry<String, GameRoom> entry : Server.gameRooms.entrySet()) {
                            writer.println(createXMLForRoomList("newGameRoom", entry.getValue()));
                        }
                    }
                } else if (document.getElementsByTagName("meta-info").item(0).getTextContent().equals("roomCreated")) {
                    for (PrintWriter writer : Server.writers) {
                        writer.println(createXMLForRoomList("newGameRoom", currentRoom));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("IOException", e);
        } catch (SAXException e) {
            logger.error("SAXException", e);
        } catch (TransformerException e) {
            logger.error("TransformerException", e);
        } finally {
            try {
                Server.writers.remove(writer);
                Server.userOnline.remove(currentPlayer.getUserName());
                writer.close();
                reader.close();
                clientSocket.close();
                boolean host = false;
                if (currentRoom.getPlayerHost() == currentPlayer) {
                    Server.gameRooms.remove(Integer.toString(currentRoom.getRoomId()));
                    host = true;
                    if (currentRoom.getRoomOnline() == 2) {
                        currentRoom.getPrintWriter().println(createXmlForHostCloseRoom());
                    }
                }
                for (PrintWriter writer : Server.writers) {
                    if (host) {
                        writer.println(createXMLForRoomList("closeRoom", currentRoom));
                    }
                    writer.println(createXMLForUserList("offline", currentPlayer));
                }
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
    }

    private Document createXML(Element inputElement, Document outputDocument) {
        Element root = outputDocument.createElement("body");
        outputDocument.appendChild(root);

        Element metaElement = outputDocument.createElement("meta-info");
        root.appendChild(metaElement);

        String meta = inputElement.getElementsByTagName("meta-info").item(0).getTextContent();
        String newMeta;
        switch (meta) {
            case "login":
                newMeta = testLogin(inputElement.getElementsByTagName("login").item(0).getTextContent(),
                        inputElement.getElementsByTagName("password").item(0).getTextContent());

                metaElement.appendChild(outputDocument.createTextNode(newMeta));

                if (!newMeta.equals("incorrect") && !newMeta.equals("currentUserOnline")) {
                    root = createUserXML(outputDocument, currentPlayer, root);
                }
                break;
            case "createRoom":
                String roomDescription = inputElement.getElementsByTagName("roomDescription").item(0).getTextContent();
                currentRoom = new GameRoom(roomDescription, currentPlayer, writer);

                Server.gameRooms.put(Integer.toString(currentRoom.getRoomId()), currentRoom);
                metaElement.appendChild(outputDocument.createTextNode("roomCreated"));

                Element roomIdElement = outputDocument.createElement("roomId");
                roomIdElement.appendChild(outputDocument.createTextNode(Integer.toString(currentRoom.getRoomId())));
                root.appendChild(roomIdElement);

                Element roomDescriptionElement = outputDocument.createElement("roomDescription");
                roomDescriptionElement.appendChild(outputDocument.createTextNode(roomDescription));
                root.appendChild(roomDescriptionElement);
                break;
            case "closeRoom":
                String idRoom = inputElement.getElementsByTagName("roomId").item(0).getTextContent();
                GameRoom closeRoom = Server.gameRooms.get(idRoom);
                Server.gameRooms.remove(idRoom);
                for (PrintWriter writer : Server.writers) {
                    writer.println(createXMLForRoomList("closeRoom", closeRoom));
                }
                if (closeRoom.getPrintWriter() != null) {
                    closeRoom.getPrintWriter().println(createXmlForHostCloseRoom());
                }
                break;
            case "changeStatus":
                String status = inputElement.getElementsByTagName("status").item(0).getTextContent();
                String roomId = inputElement.getElementsByTagName("idRoom").item(0).getTextContent();
                String playerType = inputElement.getElementsByTagName("playerType").item(0).getTextContent();
                if (playerType.equals("host")) {
                    Server.gameRooms.get(roomId).setHostStatus(status);
                } else {
                    Server.gameRooms.get(roomId).setPlayerStatus(status);
                }
                for (PrintWriter writers : Server.gameRooms.get(roomId).getWriters()) {
                    writers.println(createXMLChangeStatus(status, playerType));
                }
                break;
            case "connectToRoom":
                GameRoom gameRoom = Server.gameRooms.get(inputElement.getElementsByTagName("idRoom").item(0).getTextContent());
                if (gameRoom.getRoomOnline() != 2) {
                    metaElement.appendChild(outputDocument.createTextNode("connectionAllowed"));
                    root = createConnectAcceptXML(outputDocument, root, gameRoom);
                    PrintWriter hostWriter = Server.gameRooms.get(
                            inputElement.getElementsByTagName("idRoom").item(0).getTextContent()).getPrintWriterHost();
                    hostWriter.println(createXMLForHostAfterPlayerConnect(currentPlayer.getUserName()));
                    gameRoom.setPrintWriter(writer);
                    gameRoom.setPlayer(currentPlayer);
                    gameRoom.setRoomOnline(2);
                    for (PrintWriter writer : Server.writers) {
                        writer.println(createXMLForChangeOnlineGameRoom(gameRoom.getRoomOnline(), gameRoom.getRoomId()));
                    }
                    currentRoom = gameRoom;
                } else {
                    System.out.println("комната полна");
                }
                break;
            case "disconnectingFromRoom":
                GameRoom gameRoomDisconnect = Server.gameRooms.get(inputElement.getElementsByTagName("roomId").item(0).getTextContent());
                gameRoomDisconnect.setPlayer(null);
                gameRoomDisconnect.setPlayerStatus(null);
                gameRoomDisconnect.setPrintWriter(null);
                gameRoomDisconnect.setRoomOnline(1);
                PrintWriter hostWriter = Server.gameRooms.get(
                        inputElement.getElementsByTagName("roomId").item(0).getTextContent()).getPrintWriterHost();
                hostWriter.println(createXMLForHostAfterPlayerDisconnect());
                for (PrintWriter writer : Server.writers) {
                    writer.println(createXMLForChangeOnlineGameRoom(gameRoomDisconnect.getRoomOnline(), gameRoomDisconnect.getRoomId()));
                }
                break;

            case "startGame":
                System.out.println(currentRoom + " room, " + currentRoom.getRoomId());
                currentRoom.setGameStatus("in game");
                GameField gameField = currentRoom.getGameField();
                gameField.initGameField(Integer.parseInt(inputElement.getElementsByTagName("fieldSize").item(0).getTextContent()));
                for (PrintWriter writer : currentRoom.getWriters()) {
                    writer.println(createGameStartXML());
                }
                break;
            case "playerMove":
                gameField = currentRoom.getGameField();
                double x = Double.parseDouble(inputElement.getElementsByTagName("xCoordinate").item(0).getTextContent());
                double y = Double.parseDouble(inputElement.getElementsByTagName("yCoordinate").item(0).getTextContent());
                String color = inputElement.getElementsByTagName("playerColor").item(0).getTextContent();
                String userName = inputElement.getElementsByTagName("userName").item(0).getTextContent();
                boolean result;
                if (color.equals("BLACK")) {
                    result = gameField.isAllowedToPlace(x, y, PointState.STONE_BLACK);
                } else {
                    result = gameField.isAllowedToPlace(x, y, PointState.STONE_WHITE);
                }
                if (result) {
                    for (PrintWriter writer : currentRoom.getWriters()) {
                        if (gameField.getPointsToRemove().size() > 0) {
                            writer.println(createXMLForRemoveSet(gameField.getPointsToRemove()));
                        }
                        writer.println(createXMLForSendResultToPlayer(result, x, y, color, userName));
                    }
                    gameField.setPointsToRemoveClear();
                }
                break;
            default:
                break;
        }
        return outputDocument;

    }

    private Element createUserXML(Document document, Player player, Element root) {
        Element userName = document.createElement("userName");
        userName.appendChild(document.createTextNode(player.getUserName()));
        root.appendChild(userName);

        Element userGameCount = document.createElement("userGameCount");
        userGameCount.appendChild(document.createTextNode(player.getUserGameCount()));
        root.appendChild(userGameCount);

        Element userPercentWins = document.createElement("userPercentWins");
        userPercentWins.appendChild(document.createTextNode(player.getUserPercentWins()));
        root.appendChild(userPercentWins);

        Element userRating = document.createElement("userRating");
        userRating.appendChild(document.createTextNode(player.getUserRating()));
        root.appendChild(userRating);
        return root;
    }

    private String testLogin(String login, String password) {
        String action = "connect";
        if (!Server.userList.containsKey(login)) {
            currentPlayer = createNewUser(login, password);
        } else {
            if (!Server.userList.get(login).getUserPassword().equals(password)) {
                action = "incorrect";
            } else if (Server.userOnline.containsKey(login)) {
                action = "currentUserOnline";
            } else {
                currentPlayer = Server.userList.get(login);
            }
        }
        if (currentPlayer != null) {
            Server.userOnline.put(currentPlayer.getUserName(), currentPlayer);
        }
        return action;
    }

    private Player createNewUser(String login, String password) {
        Player newPlayer = new Player(password, login);
        Server.userList.put(login, newPlayer);
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

        Element gameCount = doc.createElement("gameCount");
        gameCount.appendChild(doc.createTextNode("0"));
        root.appendChild(gameCount);

        Element rating = doc.createElement("rating");
        rating.appendChild(doc.createTextNode("0"));
        root.appendChild(rating);

        Element percentWins = doc.createElement("percentWins");
        percentWins.appendChild(doc.createTextNode("0%"));
        root.appendChild(percentWins);
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (TransformerException e) {
            logger.error("TransformerException", e);
        }
        return newPlayer;
    }

    private String createXMLForUserList(String action, Player player) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode(action));
        root.appendChild(meta);

        if (action.equals("online")) {
            root = createUserXML(document, player, root);
        } else if (action.equals("offline")) {
            Element userName = document.createElement("userName");
            userName.appendChild(document.createTextNode(currentPlayer.getUserName()));
            root.appendChild(userName);
        }
        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createXMLForRoomList(String action, GameRoom room) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode(action));
        root.appendChild(meta);

        Element roomHost = document.createElement("roomHost");
        roomHost.appendChild(document.createTextNode(room.getPlayerHost().getUserName()));
        root.appendChild(roomHost);

        Element roomDescription = document.createElement("roomDescription");
        roomDescription.appendChild(document.createTextNode(room.getRoomDescription()));
        root.appendChild(roomDescription);

        Element roomId = document.createElement("roomId");
        roomId.appendChild(document.createTextNode(Integer.toString(room.getRoomId())));
        root.appendChild(roomId);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createXMLChangeStatus(String status, String playerType) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("changeStatus"));
        root.appendChild(meta);

        Element statusElement = document.createElement("status");
        statusElement.appendChild(document.createTextNode(status));
        root.appendChild(statusElement);

        Element playerTypeElement = document.createElement("playerType");
        playerTypeElement.appendChild(document.createTextNode(playerType));
        root.appendChild(playerTypeElement);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private Element createConnectAcceptXML(Document document, Element root, GameRoom gameRoom) {
        Element hostName = document.createElement("hostName");
        hostName.appendChild(document.createTextNode(gameRoom.getPlayerHost().getUserName()));
        root.appendChild(hostName);

        Element hostStatus = document.createElement("hostStatus");
        hostStatus.appendChild(document.createTextNode(gameRoom.getHostStatus()));
        root.appendChild(hostStatus);

        Element roomDescription = document.createElement("roomDescription");
        roomDescription.appendChild(document.createTextNode(gameRoom.getRoomDescription()));
        root.appendChild(roomDescription);

        Element roomId = document.createElement("roomId");
        roomId.appendChild(document.createTextNode(Integer.toString(gameRoom.getRoomId())));
        root.appendChild(roomId);
        return root;
    }

    private String createXMLForHostAfterPlayerConnect(String playerName) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("playerConnectToRoom"));
        root.appendChild(meta);

        Element playerNameElement = document.createElement("playerName");
        playerNameElement.appendChild(document.createTextNode(playerName));
        root.appendChild(playerNameElement);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createXMLForChangeOnlineGameRoom(int online, int roomId) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("changeOnline"));
        root.appendChild(meta);

        Element playerOnline = document.createElement("playerOnline");
        playerOnline.appendChild(document.createTextNode(Integer.toString(online)));
        root.appendChild(playerOnline);

        Element roomIdElement = document.createElement("roomId");
        roomIdElement.appendChild(document.createTextNode(Integer.toString(roomId)));
        root.appendChild(roomIdElement);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createXMLForHostAfterPlayerDisconnect() {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("playerDisconnect"));
        root.appendChild(meta);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createXmlForHostCloseRoom() {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("hostCloseRoom"));
        root.appendChild(meta);

        currentRoom = null;

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createGameStartXML() {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("startGame"));
        root.appendChild(meta);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private String createXMLForSendResultToPlayer(boolean res, double x, double y, String color, String userName) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("resultMove"));
        root.appendChild(meta);

        Element result = document.createElement("result");
        result.appendChild(document.createTextNode(Boolean.toString(res)));
        root.appendChild(result);

        Element xCoordinate = document.createElement("xCoordinate");
        xCoordinate.appendChild(document.createTextNode(Double.toString(x)));
        root.appendChild(xCoordinate);

        Element yCoordinate = document.createElement("yCoordinate");
        yCoordinate.appendChild(document.createTextNode(Double.toString(y)));
        root.appendChild(yCoordinate);

        Element playerColor = document.createElement("playerColor");
        playerColor.appendChild(document.createTextNode(color));
        root.appendChild(playerColor);

        Element blockUser = document.createElement("blockUser");
        blockUser.appendChild(document.createTextNode(userName));
        root.appendChild(blockUser);

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public String createXMLForRemoveSet(Set<Point> set) {
        Document document = builder.newDocument();

        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode("removePoint"));
        root.appendChild(meta);

        Element pointSet = document.createElement("pointSet");
        root.appendChild(pointSet);

        int id = 1;
        for (Point temp : set) {
            Element coordinate = document.createElement("coordinate");
            coordinate.setAttribute("id",Integer.toString(id++));
            coordinate.setAttribute("xCoordinate",Double.toString(temp.getX()));
            coordinate.setAttribute("yCoordinate",Double.toString(temp.getY()));
            pointSet.appendChild(coordinate);
        }

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        System.out.println(stringWriter.toString());
        return stringWriter.toString();
    }
}

