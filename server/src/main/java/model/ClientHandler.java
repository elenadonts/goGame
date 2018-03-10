package model;

import controller.Server;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;

/**
 * Handler class for create new thread when user connect
 * on server part
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class ClientHandler extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class);
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket clientSocket;
    private DocumentBuilder builder;
    private Player currentPlayer;
    private GameRoom currentRoom;

    public ClientHandler(Socket client) {
        this.clientSocket = client;
        TransformerXML.createTransformer();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("ParserConfigurationException", e);
        }
        this.setDaemon(true);
    }

    /**
     * Starts when user connecting to server.
     * Reads xml from user and send in main method
     * createXML()
     */
    @Override
    public void run() {
        LOGGER.info("User: " + clientSocket.getInetAddress().toString().replace("/", "") + " connected;");
        String input;
        try {
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while ((input = reader.readLine()) != null) {

                Document document = builder.parse(new InputSource(new StringReader(input)));
                Node user = document.getElementsByTagName("body").item(0);

                document = builder.newDocument();
                document = createXML((Element) user, document);

                StringWriter stringWriter = TransformerXML.transform(document);

                if (!document.getElementsByTagName("meta-info").item(0).getTextContent().equals("")) {
                    writer.println(stringWriter.toString());
                }

                if (document.getElementsByTagName("meta-info").item(0).getTextContent().equals("connect")) {
                    Server.writers.add(writer);
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
        } catch (SocketException e) {
            LOGGER.info("User disconnected");
        } catch (IOException | SAXException e) {
            LOGGER.error(e);
        } finally {
            try {
                Server.writers.remove(writer);
                Server.userOnline.remove(currentPlayer.getUserName());
                writer.close();
                reader.close();
                clientSocket.close();
                if (currentRoom != null && currentRoom.getGameStatus().equals("in game") && clientSocket.isConnected()) {
                    if (currentRoom.getPlayerHost().getUserName().equals(currentPlayer.getUserName())) {
                        int white = 10;
                        int black = 0;
                        changerXMLAfterGameEnd(white, black);
                        currentRoom.getPrintWriter().println(createXMLGameOver(white, black,
                                currentRoom.getPlayer().getUserName()));
                        for (PrintWriter writer : Server.writers) {
                            if (currentRoom != null) {
                                writer.println(createXMLForRoomList("closeRoom", currentRoom));
                            }
                            writer.println(createXMLForUserList("offline", currentPlayer));
                        }
                        Server.gameRooms.remove(Integer.toString(currentRoom.getRoomId()));
                    } else {
                        int white = 0;
                        int black = 10;
                        changerXMLAfterGameEnd(white, black);
                        currentRoom.getPrintWriterHost().println(createXMLGameOver(white, black,
                                currentRoom.getPlayerHost().getUserName()));
                        for (PrintWriter writer : Server.writers) {
                            writer.println(createXMLForUserList("offline", currentPlayer));
                        }
                    }
                } else if (currentRoom != null && currentRoom.getPlayerHost() == currentPlayer) {
                    Server.gameRooms.remove(Integer.toString(currentRoom.getRoomId()));
                    for (PrintWriter writer : Server.writers) {
                        if (currentRoom != null) {
                            writer.println(createXMLForRoomList("closeRoom", currentRoom));
                        }
                        writer.println(createXMLForUserList("offline", currentPlayer));
                    }
                    if (currentRoom.getRoomOnline() == 2) {
                        currentRoom.getPrintWriter().println(createXMLWithMeta("hostCloseRoom"));
                    }
                } else {
                    for (PrintWriter writer : Server.writers) {
                        writer.println(createXMLForUserList("offline", currentPlayer));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("IOException", e);
            }
        }
    }

    /**
     * Method take xml from client, read meta-info in xml, and
     * decide what need do
     *
     * @param inputElement   xml form client
     * @param outputDocument document with xml for client
     * @return document with rules
     */
    private Document createXML(Element inputElement, Document outputDocument) throws IOException, SAXException {
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
                if (!newMeta.equals("incorrect") && !newMeta.equals("currentUserOnline") && !newMeta.equals("banned")) {
                    root = createUserXML(outputDocument, currentPlayer, root);
                    Element admin = outputDocument.createElement("admin");
                    admin.appendChild(outputDocument.createTextNode(Boolean.toString(currentPlayer.isAdmin())));
                    root.appendChild(admin);
                }
                break;
            case "createRoom":
                String roomDescription = inputElement.getElementsByTagName("roomDescription").item(0).getTextContent();
                currentRoom = new GameRoom(roomDescription, currentPlayer, writer);
                currentRoom.setFieldSizeId(inputElement.getElementsByTagName("fieldSize").item(0).getTextContent());

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
                if (closeRoom.getGameStatus().equals("in game")) {
                    int white = 10;
                    int black = 0;
                    changerXMLAfterGameEnd(white, black);
                    for (PrintWriter writer : closeRoom.getWriters()) {
                        writer.println(createXMLGameOver(white, black, closeRoom.getPlayer().getUserName()));
                    }
                }
                Server.gameRooms.remove(idRoom);
                for (PrintWriter writer : Server.writers) {
                    writer.println(createXMLForRoomList("closeRoom", closeRoom));
                }
                if (closeRoom.getPrintWriter() != null) {
                    closeRoom.getPrintWriter().println(createXMLWithMeta("hostCloseRoom"));
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
                String id = inputElement.getElementsByTagName("idRoom").item(0).getTextContent();
                if (Server.gameRooms.get(id).getRoomOnline() != 2) {
                    metaElement.appendChild(outputDocument.createTextNode("connectionAllowed"));

                    Element fieldSizeId = outputDocument.createElement("fieldSizeId");
                    fieldSizeId.appendChild(outputDocument.createTextNode(Server.gameRooms.get(id).getFieldSizeId()));
                    root.appendChild(fieldSizeId);

                    createConnectAcceptXML(outputDocument, root, Server.gameRooms.get(id));

                    PrintWriter hostWriter = Server.gameRooms.get(
                            inputElement.getElementsByTagName("idRoom").item(0).getTextContent()).getPrintWriterHost();
                    hostWriter.println(createXMLForHostAfterPlayerConnect(currentPlayer.getUserName()));
                    Server.gameRooms.get(id).setPrintWriter(writer);
                    Server.gameRooms.get(id).setPlayer(currentPlayer);
                    Server.gameRooms.get(id).setRoomOnline(2);
                    for (PrintWriter writer : Server.writers) {
                        writer.println(createXMLForChangeOnlineGameRoom(Server.gameRooms.get(id).getRoomOnline(), Server.gameRooms.get(id).getRoomId()));
                    }
                    currentRoom = Server.gameRooms.get(id);
                } else {
                    metaElement.appendChild(outputDocument.createTextNode("roomFull"));
                }
                break;
            case "disconnectingFromRoom":
                GameRoom gameRoomDisconnect = Server.gameRooms.get(inputElement.getElementsByTagName("roomId").item(0).getTextContent());
                if (!gameRoomDisconnect.getGameStatus().equals("in game")) {
                    gameRoomDisconnect.setPlayer(null);
                    gameRoomDisconnect.setPrintWriter(null);
                    gameRoomDisconnect.setPlayerStatus(null);
                } else {
                    int white = 0;
                    int black = 10;
                    changerXMLAfterGameEnd(white, black);
                    for (PrintWriter writer : currentRoom.getWriters()) {
                        writer.println(createXMLGameOver(white, black, currentRoom.getPlayerHost().getUserName()));
                    }
                    for (PrintWriter temp : Server.writers) {
                        temp.println(createXMLForRoomList("closeRoom", currentRoom));
                    }
                }
                gameRoomDisconnect.setRoomOnline(1);
                PrintWriter hostWriter = Server.gameRooms.get(
                        inputElement.getElementsByTagName("roomId").item(0).getTextContent()).getPrintWriterHost();
                hostWriter.println(createXMLWithMeta("playerDisconnect"));
                for (PrintWriter writer : Server.writers) {
                    writer.println(createXMLForChangeOnlineGameRoom(gameRoomDisconnect.getRoomOnline(), gameRoomDisconnect.getRoomId()));
                }
                break;

            case "startGame":
                Server.gameRooms.get(Integer.toString(currentRoom.getRoomId())).setGameStatus("in game");
                currentRoom.setGameStatus("in game");
                GameField gameField = currentRoom.getGameField();
                int numberOfTiles = Integer.parseInt(inputElement.getElementsByTagName("numberOfTiles").item(0).getTextContent());
                gameField.initTileSize(numberOfTiles);
                gameField.initGameField(numberOfTiles);
                for (PrintWriter writer : currentRoom.getWriters()) {
                    writer.println(createGameStartXML(numberOfTiles, gameField.getTileSize()));
                }
                for (PrintWriter writer : Server.writers) {
                    writer.println(createXMLForChangeStatusGameRoom(currentRoom.getGameStatus(), currentRoom.getRoomId()));
                }
                break;
            case "playerMove":
                gameField = currentRoom.getGameField();
                double x = Double.parseDouble(inputElement.getElementsByTagName("xCoordinate").item(0).getTextContent());
                double y = Double.parseDouble(inputElement.getElementsByTagName("yCoordinate").item(0).getTextContent());
                String color = inputElement.getElementsByTagName("playerColor").item(0).getTextContent();
                String userName = inputElement.getElementsByTagName("userName").item(0).getTextContent();
                String secondUserName;
                if (currentRoom.getPlayerHost().getUserName().equals(userName)) {
                    secondUserName = currentRoom.getPlayer().getUserName();
                    if (currentRoom.isHostPassed()) {
                        currentRoom.setHostPassed(false);
                    }
                } else {
                    secondUserName = currentRoom.getPlayerHost().getUserName();
                    if (currentRoom.isPlayerPassed()) {
                        currentRoom.setPlayerPassed(false);
                    }
                }
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
                        writer.println(createXMLForSendResultToPlayer(result, x, y, color, userName, secondUserName));
                    }
                }
                gameField.setPointsToRemoveClear();

                break;
            case "changeFieldSize":
                String buttonId = inputElement.getElementsByTagName("buttonId").item(0).getTextContent();
                if (currentRoom.getPrintWriter() != null) {
                    currentRoom.getPrintWriter().println(createXMLChangeFieldSize(buttonId));
                }
                currentRoom.setFieldSizeId(buttonId);
                break;
            case "playerPassed":
                String user = inputElement.getElementsByTagName("userName").item(0).getTextContent();
                if (currentRoom.getPlayerHost().getUserName().equals(user)) {
                    currentRoom.setHostPassed(true);
                    currentRoom.getPrintWriter().println(createXMLPlayerPass(user));
                } else {
                    currentRoom.setPlayerPassed(true);
                    currentRoom.getPrintWriterHost().println(createXMLPlayerPass(user));
                }
                if (currentRoom.isHostPassed() && currentRoom.isPlayerPassed()) {
                    Server.gameRooms.remove(Integer.toString(currentRoom.getRoomId()));
                    currentRoom.getGameField().countPlayersScore();
                    int white = currentRoom.getGameField().getWhiteCount();
                    int black = currentRoom.getGameField().getBlackCount();
                    String winName = "";
                    if (white > black) {
                        winName = currentRoom.getPlayer().getUserName();
                    } else if (black > white) {
                        winName = currentRoom.getPlayerHost().getUserName();
                    }
                    changerXMLAfterGameEnd(white, black);
                    for (PrintWriter writer : currentRoom.getWriters()) {
                        writer.println(createXMLGameOver(white, black, winName));
                    }
                    for (PrintWriter writer : Server.writers) {
                        writer.println(createXMLForRoomList("closeRoom", currentRoom));
                    }
                }
                break;
            case "banUser":
                String banUserName = inputElement.getElementsByTagName("userName").item(0).getTextContent();
                if (!banUserName.equals(currentPlayer.getUserName())) {
                    Player banUser = Server.userOnline.get(banUserName);

                    File file = new File("users" + File.separator + banUserName + ".xml");
                    Document document = builder.parse(file);

                    document.getElementsByTagName("banned").item(0).setTextContent("true");


                    TransformerXML.transformToFile(document, file);
                    Server.banList.add(banUserName);
                    banUser.getWriter().println(createXMLWithMeta("ban"));
                }
                break;
            case "nullCurrentRoom":
                currentRoom = null;
                break;
            default:
                break;
        }
        return outputDocument;

    }

    /**
     * Creates xml for client with info about this user from server
     *
     * @param document for client
     * @param player   info about this user
     * @param root     element for document
     */
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


    /**
     * Tests info from client, and
     * returns error or create new user
     *
     * @param login    for test
     * @param password for test
     * @return command for client
     */
    private String testLogin(String login, String password) {
        String action = "connect";
        if (!Server.userList.containsKey(login)) {
            currentPlayer = createNewUser(login, password);
        } else {
            for (String name : Server.banList) {
                if (name.equals(login)) {
                    currentPlayer = Server.userList.get(login);
                    return "banned";
                }
            }
            if (!Server.userList.get(login).getUserPassword().equals(password)) {
                action = "incorrect";
            } else {
                if (Server.userOnline.containsKey(login)) {
                    action = "currentUserOnline";
                } else {
                    currentPlayer = Server.userList.get(login);
                    currentPlayer.setWriter(writer);
                }
            }
        }
        if (currentPlayer != null) {
            Server.userOnline.put(currentPlayer.getUserName(), currentPlayer);
        }
        return action;
    }

    /**
     * Creates new user with current login and password
     * and return new player object
     *
     * @param login    for new user
     * @param password for new user
     * @return new player object
     */
    private Player createNewUser(String login, String password) {
        Player newPlayer = new Player(password, login);
        newPlayer.setWriter(writer);
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
        rating.appendChild(doc.createTextNode("100"));
        root.appendChild(rating);

        Element percentWins = doc.createElement("percentWins");
        percentWins.appendChild(doc.createTextNode("0"));
        root.appendChild(percentWins);

        Element winGames = doc.createElement("winGames");
        winGames.appendChild(doc.createTextNode("0"));
        root.appendChild(winGames);

        Element admin = doc.createElement("admin");
        admin.appendChild(doc.createTextNode("false"));
        root.appendChild(admin);

        Element banned = doc.createElement("banned");
        banned.appendChild(doc.createTextNode("false"));
        root.appendChild(banned);

        TransformerXML.transformToFile(doc, file);

        return newPlayer;
    }

    /**
     * Creates xml for client with new info about player
     * and action what need do with player,
     * returns xml in string format
     *
     * @param action online or offline
     * @param player info
     * @return xml in string format
     */
    private String createXMLForUserList(String action, Player player) {
        Document document = builder.newDocument();

        Element root = createXML(document, action);

        if (action.equals("online")) {
            root = createUserXML(document, player, root);
        } else if (action.equals("offline")) {
            Element userName = document.createElement("userName");
            userName.appendChild(document.createTextNode(player.getUserName()));
            root.appendChild(userName);
        }

        StringWriter stringWriter = TransformerXML.transform(document);

        return stringWriter.toString();
    }

    /**
     * Creates xml for client with new info about game room
     * and action what need do with game room,
     * returns xml in string format
     *
     * @param action online or offline
     * @param room   info
     * @return xml in string format
     */
    private String createXMLForRoomList(String action, GameRoom room) {
        Document document = builder.newDocument();

        Element root = createXML(document, action);

        Element roomHost = document.createElement("roomHost");
        roomHost.appendChild(document.createTextNode(room.getPlayerHost().getUserName()));
        root.appendChild(roomHost);

        Element roomDescription = document.createElement("roomDescription");
        roomDescription.appendChild(document.createTextNode(room.getRoomDescription()));
        root.appendChild(roomDescription);

        Element roomId = document.createElement("roomId");
        roomId.appendChild(document.createTextNode(Integer.toString(room.getRoomId())));
        root.appendChild(roomId);

        Element roomOnline = document.createElement("roomOnline");
        roomOnline.appendChild(document.createTextNode(Integer.toString(room.getRoomOnline())));
        root.appendChild(roomOnline);

        Element gameStatus = document.createElement("gameStatus");
        gameStatus.appendChild(document.createTextNode(room.getGameStatus()));
        root.appendChild(gameStatus);

        StringWriter stringWriter = TransformerXML.transform(document);

        return stringWriter.toString();
    }

    /**
     * Creates xml for client with new info about status
     * player in game room,
     * returns xml in string format
     *
     * @param status     new info
     * @param playerType player type (host or simple player)
     * @return xml in string format
     */
    private String createXMLChangeStatus(String status, String playerType) {
        Document document = builder.newDocument();

        Element root = createXML(document, "changeStatus");

        Element statusElement = document.createElement("status");
        statusElement.appendChild(document.createTextNode(status));
        root.appendChild(statusElement);

        Element playerTypeElement = document.createElement("playerType");
        playerTypeElement.appendChild(document.createTextNode(playerType));
        root.appendChild(playerTypeElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for client what connect accept
     *
     * @param document for client
     * @param gameRoom info about this game room
     * @param root     element for document
     */
    private void createConnectAcceptXML(Document document, Element root, GameRoom gameRoom) {
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
    }

    /**
     * Creates xml for client, that be a host in room,
     * about the fact that the new player joined
     * to room.
     * Returns xml in string format
     *
     * @param playerName new info
     * @return xml in string format
     */
    private String createXMLForHostAfterPlayerConnect(String playerName) {
        Document document = builder.newDocument();

        Element root = createXML(document, "playerConnectToRoom");

        Element playerNameElement = document.createElement("playerName");
        playerNameElement.appendChild(document.createTextNode(playerName));
        root.appendChild(playerNameElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for all clients, that in game room changed online
     *
     * @param online new info
     * @param roomId id room
     * @return xml in string format
     */
    private String createXMLForChangeOnlineGameRoom(int online, int roomId) {
        Document document = builder.newDocument();

        Element root = createXML(document, "changeOnline");

        Element playerOnline = document.createElement("playerOnline");
        playerOnline.appendChild(document.createTextNode(Integer.toString(online)));
        root.appendChild(playerOnline);

        Element roomIdElement = document.createElement("roomId");
        roomIdElement.appendChild(document.createTextNode(Integer.toString(roomId)));
        root.appendChild(roomIdElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for clients in game room,
     * what one from them changed status
     *
     * @param status new info
     * @param roomId id room
     * @return xml in string format
     */
    private String createXMLForChangeStatusGameRoom(String status, int roomId) {
        Document document = builder.newDocument();

        Element root = createXML(document, "changeStatusGameRoom");

        Element statusElement = document.createElement("status");
        statusElement.appendChild(document.createTextNode(status));
        root.appendChild(statusElement);

        Element roomIdElement = document.createElement("roomId");
        roomIdElement.appendChild(document.createTextNode(Integer.toString(roomId)));
        root.appendChild(roomIdElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for client with simple meta info like
     * 'ban', 'playerDisconnect' or 'hostCloseRoom'
     *
     * @param meta information for client
     * @return xml in string format
     */
    private String createXMLWithMeta(String meta) {
        Document document = builder.newDocument();
        createXML(document, meta);
        return TransformerXML.transform(document).toString();
    }

    /**
     * Creates xml for clients in game room, what
     * that game starts with field size
     *
     * @param numberOfTiles field size
     * @param tileSize      tile size for client field
     * @return xml in string format
     */
    private String createGameStartXML(int numberOfTiles, double tileSize) {
        Document document = builder.newDocument();

        Element root = createXML(document, "startGame");

        Element numberOfTilesElement = document.createElement("numberOfTiles");
        numberOfTilesElement.appendChild(document.createTextNode(Integer.toString(numberOfTiles)));
        root.appendChild(numberOfTilesElement);

        Element tileSizeElement = document.createElement("tileSize");
        tileSizeElement.appendChild(document.createTextNode(Double.toString(tileSize)));
        root.appendChild(tileSizeElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }


    /**
     * Creates xml for clients in game room, what
     * that one from two player do move on coordinate,
     * and second player can move
     *
     * @param res             true or false
     * @param x               coordinate
     * @param y               coordinate
     * @param color           player (white ot black)
     * @param userName        the player who made the move
     * @param unblockUserName the player who can move
     * @return xml in string format
     */
    private String createXMLForSendResultToPlayer(boolean res, double x, double y, String color,
                                                  String userName, String unblockUserName) {
        Document document = builder.newDocument();

        Element root = createXML(document, "resultMove");

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

        Element unblockUser = document.createElement("unblockUser");
        unblockUser.appendChild(document.createTextNode(unblockUserName));
        root.appendChild(unblockUser);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for clients in game room,
     * what need remove point sets from games
     * field
     *
     * @param set point for remove
     * @return xml in string format
     */
    private String createXMLForRemoveSet(Set<Point> set) {
        Document document = builder.newDocument();

        Element root = createXML(document, "removePoint");

        Element pointSet = document.createElement("pointSet");
        root.appendChild(pointSet);

        int id = 1;
        for (Point temp : set) {
            Element coordinate = document.createElement("coordinate");
            coordinate.setAttribute("id", Integer.toString(id++));
            coordinate.setAttribute("xCoordinate", Double.toString(temp.getX()));
            coordinate.setAttribute("yCoordinate", Double.toString(temp.getY()));
            pointSet.appendChild(coordinate);
        }

        StringWriter stringWriter = TransformerXML.transform(document);

        return stringWriter.toString();
    }

    /**
     * Creates xml for simple player in game room,
     * that host changed the field size
     *
     * @param id radio button for player
     * @return xml in string format
     */
    private String createXMLChangeFieldSize(String id) {
        Document document = builder.newDocument();

        Element root = createXML(document, "changeFieldSize");

        Element radioButtonId = document.createElement("radioButtonId");
        radioButtonId.appendChild(document.createTextNode(id));
        root.appendChild(radioButtonId);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for clients in game room,
     * what one from them pass
     *
     * @param userName name player who passed
     * @return xml in string format
     */
    private String createXMLPlayerPass(String userName) {
        Document document = builder.newDocument();

        Element root = createXML(document, "playerPassed");

        Element userNameElement = document.createElement("userName");
        userNameElement.appendChild(document.createTextNode(userName));
        root.appendChild(userNameElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Creates xml for clients in game room
     * that game end
     *
     * @param userName name player who win game
     * @param black    host count score
     * @param white    simple player count score
     * @return xml in string format
     */
    private String createXMLGameOver(int white, int black, String userName) {
        Document document = builder.newDocument();

        Element root = createXML(document, "gameOver");

        Element whiteElement = document.createElement("white");
        whiteElement.appendChild(document.createTextNode(Integer.toString(white)));
        root.appendChild(whiteElement);

        Element blackElement = document.createElement("black");
        blackElement.appendChild(document.createTextNode(Integer.toString(black)));
        root.appendChild(blackElement);

        Element userNameElement = document.createElement("userName");
        userNameElement.appendChild(document.createTextNode(userName));
        root.appendChild(userNameElement);

        StringWriter stringWriter = TransformerXML.transform(document);
        return stringWriter.toString();
    }

    /**
     * Change xml on server with new info
     * after game end
     *
     * @param black host count score
     * @param white simple player count score
     */
    private void changerXMLAfterGameEnd(int white, int black) {
        int res;
        String hostName = currentRoom.getPlayerHost().getUserName();
        String userName = currentRoom.getPlayer().getUserName();
        if (white > black) {
            res = white - black;
            setNewInfoAboutUser(userName, res);
            setNewInfoAboutUser(hostName, -res);
        } else if (black > white) {
            res = black - white;
            setNewInfoAboutUser(hostName, res);
            setNewInfoAboutUser(userName, -res);
        } else {
            setNewInfoAboutUser(hostName, 0);
            setNewInfoAboutUser(userName, 0);
        }
    }

    /**
     * Change xml on server about user after game end
     *
     * @param name player for change
     * @param res  result player in game
     */
    private void setNewInfoAboutUser(String name, int res) {
        try {
            File file = new File("users" + File.separator + name + ".xml");
            Document document = builder.parse(file);

            int gameCount = Integer.parseInt(document.getElementsByTagName("gameCount").item(0).getTextContent());
            document.getElementsByTagName("gameCount").item(0).setTextContent(Integer.toString(++gameCount));

            int rating = Integer.parseInt(document.getElementsByTagName("rating").item(0).getTextContent());
            rating += res;
            document.getElementsByTagName("rating").item(0).setTextContent(Integer.toString(rating));

            int winGames = Integer.parseInt(document.getElementsByTagName("winGames").item(0).getTextContent());
            if (res > 0) {
                document.getElementsByTagName("winGames").item(0).setTextContent(Integer.toString(++winGames));
            }
            double percentWins = winGames * 100 / gameCount;
            document.getElementsByTagName("percentWins").item(0).setTextContent(Double.toString(percentWins));

            TransformerXML.transformToFile(document, file);

            for (PrintWriter temp : Server.writers) {
                Player player = Server.userList.get(name);
                player.setUserGameCount(Integer.toString(gameCount));
                player.setUserRating(Integer.toString(rating));
                player.setUserWinGames(Integer.toString(winGames));
                player.setUserPercentWins(Double.toString(percentWins));
                temp.println(createXMLForNewUserInfo(player));
            }
        } catch (SAXException | IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Creates xml for all clients what some player
     * changed info about yourself,
     *
     * @param player with new info for all clients
     * @return xml in string format
     */
    private String createXMLForNewUserInfo(Player player) {
        Document document = builder.newDocument();

        Element root = createXML(document, "newUserInfo");

        Element name = document.createElement("userName");
        name.appendChild(document.createTextNode(player.getUserName()));
        root.appendChild(name);

        Element gameCount = document.createElement("gameCount");
        gameCount.appendChild(document.createTextNode(player.getUserGameCount()));
        root.appendChild(gameCount);

        Element rating = document.createElement("rating");
        rating.appendChild(document.createTextNode(player.getUserRating()));
        root.appendChild(rating);

        Element percentWins = document.createElement("percentWins");
        percentWins.appendChild(document.createTextNode(player.getUserPercentWins()));
        root.appendChild(percentWins);

        StringWriter stringWriter = TransformerXML.transform(document);

        return stringWriter.toString();
    }

    /**
     * Create root xml element
     * and meta-info element with values
     *
     * @param document for creating element
     * @param metaInfo values for meta-info element
     * @return root element
     */
    private Element createXML(Document document, String metaInfo) {
        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode(metaInfo));
        root.appendChild(meta);

        return root;
    }
}

