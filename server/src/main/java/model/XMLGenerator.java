package model;

import controller.Server;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that read xml from player
 * and create xml answer
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class XMLGenerator {
    private static final Logger LOGGER = Logger.getLogger(XMLGenerator.class);
    private Player currentPlayer;
    private GameRoom currentRoom;
    private PrintWriter writer;

    /**
     * Sets print writer
     *
     * @param writer the new info
     */
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * Reads xml from user and create answer
     *
     * @param input xml for read
     */
    public void readInput(String input) {
        try {
            Document document = TransformerXML.getDocumentBuilder().parse(new InputSource(new StringReader(input)));
            Node user = document.getElementsByTagName("body").item(0);

            document = TransformerXML.newDocument();
            document = createXML((Element) user, document);


            if (!document.getElementsByTagName("meta-info").item(0).getTextContent().equals("")) {
                writer.println(TransformerXML.transformToString(document));
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
        } catch (SAXException | IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Action when player exit from game
     */
    public void checkAfterPlayerExit() {
        Server.writers.remove(writer);
        if (playerAlreadyLoggedIn()) {
            Server.userOnline.remove(currentPlayer.getUserName());
        }
        if (currentRoom != null && currentRoom.getGameStatus().equals("in game")) {
            if (currentRoom.getPlayerHost().getUserName().equals(currentPlayer.getUserName())) {
                int white = 10;
                int black = 0;
                changerXMLAfterGameEnd(white, black);
                currentRoom.getPrintWriter().println(createXMLGameOver(white, black,
                        currentRoom.getPlayer().getUserName()));
                for (PrintWriter writer : Server.writers) {
                    writer.println(createXMLForRoomList("closeRoom", currentRoom));
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
                writer.println(createXMLForRoomList("closeRoom", currentRoom));
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
    }

    /**
     * Check player on logged in
     */
    private boolean playerAlreadyLoggedIn() {
        return currentPlayer != null;
    }

    /**
     * Method take xml from client, read meta-info in xml, and
     * decide what need do
     *
     * @param inputElement   xml form client
     * @param outputDocument document with xml for client
     * @return document with rules
     */
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

                if (newMeta.equals("connect")) {
                    root = createUserXML(outputDocument, currentPlayer, root);
                    root.appendChild(TransformerXML.createElement(outputDocument, "admin", Boolean.toString(currentPlayer.isAdmin())));
                }
                break;
            case "createRoom":
                String roomDescription = inputElement.getElementsByTagName("roomDescription").item(0).getTextContent();
                currentRoom = new GameRoom(roomDescription, currentPlayer, writer);
                currentRoom.setFieldSizeId(inputElement.getElementsByTagName("fieldSize").item(0).getTextContent());

                Server.gameRooms.put(Integer.toString(currentRoom.getRoomId()), currentRoom);
                metaElement.appendChild(outputDocument.createTextNode("roomCreated"));

                root.appendChild(TransformerXML.createElement(outputDocument, "roomId", Integer.toString(currentRoom.getRoomId())));
                root.appendChild(TransformerXML.createElement(outputDocument, "roomDescription", roomDescription));
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
                    root.appendChild(TransformerXML.createElement(outputDocument, "fieldSizeId", Server.gameRooms.get(id).getFieldSizeId()));

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
                    Document document = TransformerXML.parseFile(file);

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
        root.appendChild(TransformerXML.createElement(document, "userName", player.getUserName()));
        root.appendChild(TransformerXML.createElement(document, "userGameCount", player.getUserGameCount()));
        root.appendChild(TransformerXML.createElement(document, "userPercentWins", player.getUserPercentWins()));
        root.appendChild(TransformerXML.createElement(document, "userRating", player.getUserRating()));
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
        Pattern pattern = Pattern.compile("\\w{4,}");
        Matcher matcher = pattern.matcher(login);
        if (!matcher.matches()) {
            return "incorrectCharInLogin";
        }
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
        Document doc = TransformerXML.newDocument();
        DOMImplementation domImpl = doc.getImplementation();
        URL schemaURL = XMLGenerator.class.getClassLoader().getResource("schema.dtd");
        DocumentType docType = domImpl.createDocumentType("doctype",
                "body",
                schemaURL.toString());
        Transformer transformer;
        try {
            transformer = TransformerXML.getTransformerFactory().newTransformer();
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
            Element root = doc.createElement("body");
            doc.appendChild(root);

            root.appendChild(TransformerXML.createElement(doc, "login", login));
            root.appendChild(TransformerXML.createElement(doc, "password", password));
            root.appendChild(TransformerXML.createElement(doc, "gameCount", "0"));
            root.appendChild(TransformerXML.createElement(doc, "rating", "100"));
            root.appendChild(TransformerXML.createElement(doc, "percentWins", "0"));
            root.appendChild(TransformerXML.createElement(doc, "winGames", "0"));
            root.appendChild(TransformerXML.createElement(doc, "admin", "false"));
            root.appendChild(TransformerXML.createElement(doc, "banned", "false"));

            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, action);
        if (action.equals("online")) {
            root = createUserXML(document, player, root);
        } else if (action.equals("offline")) {
            root.appendChild(TransformerXML.createElement(document, "userName", player.getUserName()));
        }
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, action);
        root.appendChild(TransformerXML.createElement(document, "roomHost", room.getPlayerHost().getUserName()));
        root.appendChild(TransformerXML.createElement(document, "roomDescription", room.getRoomDescription()));
        root.appendChild(TransformerXML.createElement(document, "roomId", Integer.toString(room.getRoomId())));
        root.appendChild(TransformerXML.createElement(document, "roomOnline", Integer.toString(room.getRoomOnline())));
        root.appendChild(TransformerXML.createElement(document, "gameStatus", room.getGameStatus()));
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "changeStatus");
        root.appendChild(TransformerXML.createElement(document, "status", status));
        root.appendChild(TransformerXML.createElement(document, "playerType", playerType));
        return TransformerXML.transformToString(document);
    }

    /**
     * Creates xml for client what connect accept
     *
     * @param document for client
     * @param gameRoom info about this game room
     * @param root     element for document
     */
    private void createConnectAcceptXML(Document document, Element root, GameRoom gameRoom) {
        root.appendChild(TransformerXML.createElement(document, "hostName", gameRoom.getPlayerHost().getUserName()));
        root.appendChild(TransformerXML.createElement(document, "hostStatus", gameRoom.getHostStatus()));
        root.appendChild(TransformerXML.createElement(document, "roomDescription", gameRoom.getRoomDescription()));
        root.appendChild(TransformerXML.createElement(document, "roomId", Integer.toString(gameRoom.getRoomId())));
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "playerConnectToRoom");
        root.appendChild(TransformerXML.createElement(document, "playerName", playerName));
        return TransformerXML.transformToString(document);
    }

    /**
     * Creates xml for all clients, that in game room changed online
     *
     * @param online new info
     * @param roomId id room
     * @return xml in string format
     */
    private String createXMLForChangeOnlineGameRoom(int online, int roomId) {
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "changeOnline");
        root.appendChild(TransformerXML.createElement(document, "playerOnline", Integer.toString(online)));
        root.appendChild(TransformerXML.createElement(document, "roomId", Integer.toString(roomId)));
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "changeStatusGameRoom");
        root.appendChild(TransformerXML.createElement(document, "status", status));
        root.appendChild(TransformerXML.createElement(document, "roomId", Integer.toString(roomId)));
        return TransformerXML.transformToString(document);
    }

    /**
     * Creates xml for client with simple meta info like
     * 'ban', 'playerDisconnect' or 'hostCloseRoom'
     *
     * @param meta information for client
     * @return xml in string format
     */
    private String createXMLWithMeta(String meta) {
        Document document = TransformerXML.newDocument();
        createXML(document, meta);
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "startGame");
        root.appendChild(TransformerXML.createElement(document, "numberOfTiles", Integer.toString(numberOfTiles)));
        root.appendChild(TransformerXML.createElement(document, "tileSize", Double.toString(tileSize)));
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "resultMove");
        root.appendChild(TransformerXML.createElement(document, "result", Boolean.toString(res)));
        root.appendChild(TransformerXML.createElement(document, "xCoordinate", Double.toString(x)));
        root.appendChild(TransformerXML.createElement(document, "yCoordinate", Double.toString(y)));
        root.appendChild(TransformerXML.createElement(document, "playerColor", color));
        root.appendChild(TransformerXML.createElement(document, "blockUser", userName));
        root.appendChild(TransformerXML.createElement(document, "unblockUser", unblockUserName));
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
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
        return TransformerXML.transformToString(document);
    }

    /**
     * Creates xml for simple player in game room,
     * that host changed the field size
     *
     * @param id radio button for player
     * @return xml in string format
     */
    private String createXMLChangeFieldSize(String id) {
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "changeFieldSize");
        root.appendChild(TransformerXML.createElement(document, "radioButtonId", id));
        return TransformerXML.transformToString(document);
    }

    /**
     * Creates xml for clients in game room,
     * what one from them pass
     *
     * @param userName name player who passed
     * @return xml in string format
     */
    private String createXMLPlayerPass(String userName) {
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "playerPassed");
        root.appendChild(TransformerXML.createElement(document, "userName", userName));
        return TransformerXML.transformToString(document);
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
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "gameOver");
        root.appendChild(TransformerXML.createElement(document, "white", Integer.toString(white)));
        root.appendChild(TransformerXML.createElement(document, "black", Integer.toString(black)));
        root.appendChild(TransformerXML.createElement(document, "userName", userName));
        return TransformerXML.transformToString(document);
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
        File file = new File("users" + File.separator + name + ".xml");
        Document document = TransformerXML.parseFile(file);

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
    }

    /**
     * Creates xml for all clients what some player
     * changed info about yourself,
     *
     * @param player with new info for all clients
     * @return xml in string format
     */
    private String createXMLForNewUserInfo(Player player) {
        Document document = TransformerXML.newDocument();
        Element root = createXML(document, "newUserInfo");
        root.appendChild(TransformerXML.createElement(document, "userName", player.getUserName()));
        root.appendChild(TransformerXML.createElement(document, "gameCount", player.getUserGameCount()));
        root.appendChild(TransformerXML.createElement(document, "rating", player.getUserRating()));
        root.appendChild(TransformerXML.createElement(document, "percentWins", player.getUserPercentWins()));
        return TransformerXML.transformToString(document);
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
        root.appendChild(TransformerXML.createElement(document, "meta-info", metaInfo));
        return root;
    }
}
