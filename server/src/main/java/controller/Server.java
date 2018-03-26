package controller;

import model.GameRoom;
import model.Player;
import model.TransformerXML;
import model.XMLGenerator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Main class for start server
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private static final int PORT = 3000;
    public static HashMap<String, Player> userList;
    public static HashSet<PrintWriter> writers;
    public static HashMap<String, Player> userOnline;
    public static HashMap<String, GameRoom> gameRooms;
    public static HashSet<String> banList;
    private static final BufferedReader SERVER_CONSOLE = new BufferedReader(new InputStreamReader(System.in));
    private static boolean isRunning;
    private static ServerSocket server;
    private static String serverArgs;

    public static void main(String[] args) {
        if (args.length > 0) {
            serverArgs = args[0].toLowerCase();
        } else {
            serverArgs = "";
        }
        runServer();
    }

    /**
     * Start server
     */
    private static void runServer() {
        if (serverArgs.equals("stop") || serverArgs.equals("restart")) {
            Socket checkSocket = new Socket();
            int timeOut = (int) TimeUnit.SECONDS.toMillis(5); // 5 sec wait period
            PrintWriter writer = null;
            try {
                checkSocket.connect(new InetSocketAddress("localhost", PORT), timeOut);
                writer = new PrintWriter(new OutputStreamWriter(checkSocket.getOutputStream()), true);
                writer.println(serverArgs);
            } catch (ConnectException e) {
                LOGGER.error("Server doesn't exist", e);
                System.exit(0);
            } catch (IOException e) {
                LOGGER.error("Exception during executing", e);
            } finally {
                try {
                    writer.close();
                    checkSocket.close();
                } catch (IOException e) {
                    LOGGER.error("Exception during closing", e);
                }
            }
        } else {
            TransformerXML.createTransformer();
            isRunning = true;
            writers = new HashSet<>();
            userOnline = new HashMap<>();
            gameRooms = new HashMap<>();
            uploadUserList();
            try {
                server = new ServerSocket(PORT);
                server.setSoTimeout(500);
                LOGGER.info("Server starting...");
                while (isRunning) {
                    try {
                        if (SERVER_CONSOLE.ready()) {
                            ServerCommand serverCommand = getCommand(SERVER_CONSOLE.readLine());
                            handleCommand(serverCommand);
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            LOGGER.error("Exception during sleeping", e);
                        }
                        ClientHandler clientHandler = new ClientHandler(server.accept());
                        clientHandler.start();
                    } catch (SocketTimeoutException ex) {
                        continue;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Exception during executing", e);
            }
        }
    }

    /**
     * Check the input and execute corresponding command
     *
     * @param command command received from console
     */
    public static void handleCommand(ServerCommand command) {
        switch (command) {
            case STOP:
                stopServer();
                break;
            case RESTART:
                restartServer();
                break;
            case UNKNOWN_COMMAND:
                break;
        }
    }

    /**
     * Restart server
     */
    private static void restartServer() {
        LOGGER.info("Restarting server");
        for (PrintWriter writer : writers) {
            writer.println(new XMLGenerator().createXMLWithMeta("restart"));
        }
        stopServer();
        runServer();
    }

    /**
     * Stop server
     */
    private static void stopServer() {
        LOGGER.info("Stopping server");
        isRunning = false;
        try {
            server.close();
        } catch (IOException e) {
            LOGGER.error("Exception stopping server", e);
        }
    }

    /**
     * Parse command to corresponding enum value
     *
     * @param command received input
     * @return corresponding command
     */
    public static ServerCommand getCommand(String command) {
        command = command.toLowerCase();
        ServerCommand serverCommand;
        switch (command) {
            case "stop":
                serverCommand = ServerCommand.STOP;
                break;
            case "restart":
                serverCommand = ServerCommand.RESTART;
                break;
            default:
                serverCommand = ServerCommand.UNKNOWN_COMMAND;
                break;
        }
        return serverCommand;
    }

    /**
     * Upload user list from dir 'users'
     */
    private static void uploadUserList() {
        boolean isXMLBroken = false;
        File folder = new File("users/");
        folder.mkdir();
        File[] userFileLists = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        userList = new HashMap<>();
        banList = new HashSet<>();
        DocumentBuilder documentBuilder;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setValidating(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException ex) {
                    LOGGER.error("SAXParseException:error", ex);
                }

                @Override
                public void fatalError(SAXParseException ex) {
                    LOGGER.error("SAXParseException:fatalError", ex);
                }

                @Override
                public void warning(SAXParseException ex) {
                    LOGGER.error("SAXParseException:warning", ex);
                }
            });
            for (File userFile : userFileLists) {
                Document doc = documentBuilder.parse(userFile);
                Node user = doc.getElementsByTagName("body").item(0);
                Element userElement = (Element) user;
                Player player = new Player();
                String login = getTextValue(userElement, "login");
                String password = getTextValue(userElement, "password");
                String gameCount = getTextValue(userElement, "gameCount");
                String percentWins = getTextValue(userElement, "percentWins");
                String rating = getTextValue(userElement, "rating");
                String winGames = getTextValue(userElement, "winGames");
                String admin = getTextValue(userElement, "admin");
                String banned = getTextValue(userElement, "banned");
                String[] elements = new String[]{login, password, gameCount, percentWins, rating, winGames, admin, banned};
                for (String temp : elements) {
                    if (temp != null) {
                        if (temp.isEmpty()) {
                            isXMLBroken = true;
                        }
                    } else {
                        isXMLBroken = true;
                    }
                }
                if (isXMLBroken) {
                    LOGGER.warn(isXMLBroken);
                    continue;
                }
                player.setUserName(login);
                player.setUserGameCount(gameCount);
                player.setUserPassword(password);
                player.setUserPercentWins(percentWins);
                player.setUserRating(rating);
                player.setUserWinGames(winGames);
                player.setAdmin(Boolean.parseBoolean(admin));
                if (Boolean.parseBoolean(banned)) {
                    banList.add(player.getUserName());
                }
                userList.put(player.getUserName(), player);
            }
        } catch (ParserConfigurationException e) {
            LOGGER.error(e);
        } catch (SAXException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Gets text value from xml element with tag name
     *
     * @param doc the xml
     * @param tag name
     * @return string value
     */
    private static String getTextValue(Element doc, String tag) {
        String value = null;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }
}
