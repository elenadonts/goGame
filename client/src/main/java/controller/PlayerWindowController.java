package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.ClientHandler;
import model.GameRoom;
import model.Player;
import model.TransformerAndDocumentFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import view.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main controller what read xml and
 * thinks what need do with this info
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class PlayerWindowController {
    private static final Logger LOGGER = Logger.getLogger(PlayerWindowController.class);
    @FXML
    public Label helloUser;
    @FXML
    public TableColumn<String, String> userName;
    @FXML
    public TableColumn<String, String> userGameCount;
    @FXML
    public TableColumn<String, String> userRating;
    @FXML
    public TableColumn<String, String> userPercentWins;
    @FXML
    public TableColumn<String, String> userStatus;
    @FXML
    public TableView<Player> userListTable;
    @FXML
    public TableView<GameRoom> lobbyListTable;
    @FXML
    public TableColumn<String, String> lobbyHost;
    @FXML
    public TableColumn<String, String> lobbyDescription;
    @FXML
    public TableColumn<String, String> lobbyOnline;
    @FXML
    public TableColumn<String, String> lobbyStatus;
    @FXML
    public Tab privateRoomTab;
    @FXML
    public TabPane tabPane;
    @FXML
    public Button createRoomButton;
    @FXML
    public Button closeRoom;
    @FXML
    public Label labelHostNickName;
    @FXML
    public Label labelPlayerNickName;
    @FXML
    public Label labelHostStatus;
    @FXML
    public Label labelPlayerStatus;
    @FXML
    public Button connectToRoom;
    @FXML
    public Button startGame;
    @FXML
    public Pane gamePane;
    @FXML
    public Button buttonReady;
    @FXML
    public RadioButton fieldSize5;
    @FXML
    public RadioButton fieldSize6;
    @FXML
    public RadioButton fieldSize7;
    @FXML
    public RadioButton fieldSize8;
    @FXML
    public ToggleGroup filedSizeGroup;
    @FXML
    public Pane fieldSizePane;
    @FXML
    public Label timeLabel;
    @FXML
    public Button passButton;
    @FXML
    public Label playerProgressName;
    @FXML
    public Button banUser;

    private ClientHandler clientHandler = new ClientHandler();
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("mm:ss");
    private int interval;
    private Timer timer = new Timer();
    private SingleSelectionModel<Tab> gameRoomTabSelectionModel;
    private Stage loginStage = new Stage();
    private LoginController loginController;
    private ObservableList<Player> userObsList = FXCollections.observableArrayList();
    private ObservableList<GameRoom> gameRoomObsList = FXCollections.observableArrayList();
    private Player currentPlayer;
    private GameRoom currentGameRoom = new GameRoom();
    private String roomId;
    private GoGame goGame;


    /**
     * Method run when project start
     * create new clientHandler object and set
     * properties for table view, login stage and etc.
     */
    @FXML
    public void initialize() throws IOException {
        TransformerAndDocumentFactory.createTransformerAndBuilder();
        tabPane.getTabs().remove(privateRoomTab);

        clientHandler.setDaemon(true);
        clientHandler.start();
        clientHandler.setGuiController(this);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent login = fxmlLoader.load();
        loginController = fxmlLoader.getController();
        loginStage.setTitle("GoGame login");
        loginStage.setScene(new Scene(login));
        loginStage.setResizable(false);
        loginStage.initOwner(Main.mainStage);
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.show();

        userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userGameCount.setCellValueFactory(new PropertyValueFactory<>("userGameCount"));
        userRating.setCellValueFactory(new PropertyValueFactory<>("userRating"));
        userPercentWins.setCellValueFactory(new PropertyValueFactory<>("userPercentWins"));
        userStatus.setCellValueFactory(new PropertyValueFactory<>("userStatus"));
        userListTable.setItems(userObsList);

        lobbyHost.setCellValueFactory(new PropertyValueFactory<>("host"));
        lobbyDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        lobbyOnline.setCellValueFactory(new PropertyValueFactory<>("online"));
        lobbyStatus.setCellValueFactory(new PropertyValueFactory<>("statusGame"));
        lobbyListTable.setItems(gameRoomObsList);

        LoginController.setClientHandler(clientHandler);

        gameRoomTabSelectionModel = tabPane.getSelectionModel();
        gameRoomTabSelectionModel.select(privateRoomTab);

        gamePane.setDisable(true);
        passButton.setDisable(true);

        LOGGER.info("Player's window initialized");
    }

    /**
     * Method take xml from server, read meta-info in xml, and
     * decide what need do
     *
     * @param input xml form server in string format
     */
    public void readXML(String input) {
        try {
            Document document = TransformerAndDocumentFactory.getDocumentBuilder().parse(new InputSource(new StringReader(input)));

            Node user = document.getElementsByTagName("body").item(0);
            String metaInfo = ((Element) user).getElementsByTagName("meta-info").item(0).getTextContent();
            if (metaInfo.contains(";")) {
                metaInfo = metaInfo.substring(0, metaInfo.indexOf(";"));
            }
            Player player;
            GameRoom gameRoom;
            switch (metaInfo) {
                case "connect":
                    boolean admin = Boolean.parseBoolean(((Element) user).getElementsByTagName("admin").item(0).getTextContent());
                    Platform.runLater(() -> {
                        if (admin) {
                            banUser.setVisible(true);
                        }
                        Main.mainStage.show();
                        loginStage.close();
                    });
                    currentPlayer = getPlayerFromXML((Element) user);
                    userObsList.add(currentPlayer);
                    helloUser.setText("Hello " + currentPlayer.getUserName());
                    break;
                case "incorrect":
                    Platform.runLater(() ->
                            loginController.setErrorLabel("Login or password are incorrect!!!")
                    );
                    break;
                case "banned":
                    Platform.runLater(() ->
                            loginController.setErrorLabel("You were banned on this server!!!")
                    );
                    break;
                case "currentUserOnline":
                    Platform.runLater(() ->
                            loginController.setErrorLabel("Current user online now!!!")
                    );
                    break;
                case "online":
                    player = getPlayerFromXML((Element) user);
                    if (!player.getUserName().equals(currentPlayer.getUserName())) {
                        if (!checkContainsPlayer(player)) {
                            userObsList.add(player);
                        }
                    }
                    break;
                case "offline":
                    player = new Player(((Element) user).getElementsByTagName("userName").item(0).getTextContent());
                    removePlayerFromPlayersList(player);
                    break;
                case "roomCreated":
                    roomId = ((Element) user).getElementsByTagName("roomId").item(0).getTextContent();
                    currentGameRoom = new GameRoom(currentPlayer.getUserName(),
                            ((Element) user).getElementsByTagName("roomDescription").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("roomId").item(0).getTextContent());
                    addPrivateGameTab(labelHostNickName, labelHostStatus, currentPlayer.getUserName());
                    createRoomButton.disableProperty().setValue(true);
                    connectToRoom.disableProperty().setValue(true);
                    gameRoomObsList.add(currentGameRoom);
                    break;
                case "newGameRoom":
                    gameRoom = getGameRoomFromXML((Element) user);
                    if (!gameRoom.getIdRoom().equals(currentGameRoom.getIdRoom())) {
                        if (!checkContainsGameRoom(gameRoom)) {
                            gameRoomObsList.add(gameRoom);
                        }
                    }
                    break;
                case "closeRoom":
                    gameRoom = getGameRoomFromXML((Element) user);
                    removeGameRoomFromGameRoomList(gameRoom);
                    connectToRoom.disableProperty().setValue(false);
                    break;
                case "changeStatus":
                    String playerType = ((Element) user).getElementsByTagName("playerType").item(0).getTextContent();
                    String status = ((Element) user).getElementsByTagName("status").item(0).getTextContent();
                    Platform.runLater(() -> {
                        if (playerType.equals("host")) {
                            labelHostStatus.setText(status);
                            currentGameRoom.setStatusHost(status);
                        } else {
                            labelPlayerStatus.setText(status);
                            currentGameRoom.setStatusPlayer(status);
                        }
                    });
                    break;

                case "connectionAllowed":
                    roomId = ((Element) user).getElementsByTagName("roomId").item(0).getTextContent();
                    currentGameRoom = new GameRoom(((Element) user).getElementsByTagName("hostName").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("roomDescription").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("roomId").item(0).getTextContent());
                    addPrivateGameTab(labelPlayerNickName, labelPlayerStatus, currentPlayer.getUserName());
                    currentGameRoom.setPlayer(currentPlayer.getUserName());
                    Platform.runLater(() -> {
                        labelHostNickName.setText(currentGameRoom.getHost());
                        labelHostStatus.setText(((Element) user).getElementsByTagName("hostStatus").item(0).getTextContent());
                        fieldSizePane.disableProperty().set(true);
                    });
                    setRadioButtonSelected(((Element) user).getElementsByTagName("fieldSizeId").item(0).getTextContent());
                    createRoomButton.disableProperty().setValue(true);
                    connectToRoom.disableProperty().setValue(true);
                    startGame.disableProperty().setValue(true);
                    currentGameRoom.setOnline("2");
                    break;

                case "playerConnectToRoom":
                    currentGameRoom.setPlayer(((Element) user).getElementsByTagName("playerName").item(0).getTextContent());
                    Platform.runLater(() -> {
                        labelPlayerNickName.setText(((Element) user).getElementsByTagName("playerName").item(0).getTextContent());
                        labelPlayerStatus.setText("not ready");
                    });
                    currentGameRoom.setOnline("2");
                    break;
                case "changeOnline":
                    setOnlineInGameRoom(((Element) user).getElementsByTagName("playerOnline").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("roomId").item(0).getTextContent());
                    break;
                case "changeStatusGameRoom":
                    setStatusInGameRoom(((Element) user).getElementsByTagName("status").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("roomId").item(0).getTextContent());
                    break;
                case "playerDisconnect":
                    currentGameRoom.setPlayer("");
                    Platform.runLater(() -> {
                        labelPlayerNickName.setText("");
                        labelPlayerStatus.setText("");
                        currentGameRoom.setOnline("1");
                    });
                    break;
                case "hostCloseRoom":
                    currentGameRoom = new GameRoom();
                    roomId = "";
                    Platform.runLater(() -> {
                        tabPane.getTabs().remove(privateRoomTab);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Host closed this room", ButtonType.OK);
                        alert.showAndWait();
                        labelPlayerStatus.setText("");
                        labelPlayerNickName.setText("");
                        buttonReady.disableProperty().setValue(false);
                        startGame.disableProperty().setValue(false);
                        createRoomButton.disableProperty().setValue(false);
                        fieldSize5.selectedProperty().setValue(true);
                        gamePane.getChildren().clear();
                        fieldSizePane.setDisable(false);
                    });
                    goGame = null;
                    break;
                case "startGame":
                    goGame = new GoGame();
                    goGame.setNumberOfTiles(Integer.parseInt(((Element) user).getElementsByTagName("numberOfTiles").item(0).getTextContent()));
                    goGame.setTileSize(Double.parseDouble(((Element) user).getElementsByTagName("tileSize").item(0).getTextContent()));
                    Platform.runLater(() -> {
                        gamePane.getChildren().add(goGame.createContent());
                        playerProgressName.setText(currentGameRoom.getHost());
                    });
                    if (currentGameRoom.getHost().equals(currentPlayer.getUserName())) {
                        gamePane.disableProperty().set(false);
                        passButton.setDisable(false);
                    }
                    buttonReady.disableProperty().set(true);
                    Tile.setClientHandler(clientHandler);
                    Tile.setPlayerWindowController(this);
                    fieldSizePane.setDisable(true);
                    startTimer();
                    break;
                case "resultMove":
                    double x = Double.parseDouble(((Element) user).getElementsByTagName("xCoordinate").item(0).getTextContent());
                    double y = Double.parseDouble(((Element) user).getElementsByTagName("yCoordinate").item(0).getTextContent());
                    String color = ((Element) user).getElementsByTagName("playerColor").item(0).getTextContent();
                    String blockUser = ((Element) user).getElementsByTagName("blockUser").item(0).getTextContent();
                    String unblockUser = ((Element) user).getElementsByTagName("unblockUser").item(0).getTextContent();
                    if (blockUser.equals(currentPlayer.getUserName())) {
                        gamePane.disableProperty().set(true);
                        passButton.setDisable(true);
                        startTimer();
                    } else {
                        gamePane.disableProperty().set(false);
                        passButton.setDisable(false);
                        startTimer();
                    }
                    StoneColor stoneColor;
                    if (color.equals("BLACK")) {
                        stoneColor = StoneColor.BLACK;
                    } else {
                        stoneColor = StoneColor.WHITE;
                    }
                    Stone stone = new Stone(stoneColor, x, y, goGame.getTileSize());
                    LastStone lastStone = new LastStone(x, y, goGame.getTileSize());
                    Platform.runLater(() -> {
                        playerProgressName.setText(unblockUser);
                        if (goGame.getLastStone() != null) {
                            goGame.removeLastStone();
                        }
                        goGame.drawStone(stone);
                        goGame.drawLastStone(lastStone);
                    });
                    break;
                case "removePoint":
                    NodeList nodeList = document.getElementsByTagName("coordinate");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        double xCoordinate = Double.parseDouble(nodeList.item(i).getAttributes().getNamedItem("xCoordinate").getTextContent());
                        double yCoordinate = Double.parseDouble(nodeList.item(i).getAttributes().getNamedItem("yCoordinate").getTextContent());
                        Platform.runLater(() -> goGame.removeStone(xCoordinate, yCoordinate));
                    }
                    break;
                case "changeFieldSize":
                    setRadioButtonSelected(((Element) user).getElementsByTagName("radioButtonId").item(0).getTextContent());
                    break;
                case "playerPassed":
                    Platform.runLater(() -> playerProgressName.setText(currentPlayer.getUserName()));
                    startTimer();
                    gamePane.setDisable(false);
                    passButton.setDisable(false);
                    break;
                case "gameOver":
                    int white = Integer.parseInt(((Element) user).getElementsByTagName("white").item(0).getTextContent());
                    int black = Integer.parseInt(((Element) user).getElementsByTagName("black").item(0).getTextContent());
                    String res;
                    String winName = ((Element) user).getElementsByTagName("userName").item(0).getTextContent();
                    if (white > black) {
                        res = "Win: " + winName + " with " + (white - black) + " points";
                    } else if (black > white) {
                        res = "Win: " + winName + " with " + (black - white) + " points";
                    } else {
                        res = "Draw";
                    }
                    setNullCurrentRoom();
                    Platform.runLater(() -> {
                        tabPane.getTabs().remove(privateRoomTab);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, res, ButtonType.OK);
                        alert.showAndWait();
                        labelPlayerStatus.setText("");
                        labelPlayerNickName.setText("");
                        buttonReady.disableProperty().setValue(false);
                        startGame.disableProperty().setValue(false);
                        createRoomButton.disableProperty().setValue(false);
                        fieldSize5.selectedProperty().setValue(true);
                        gamePane.getChildren().clear();
                        fieldSizePane.setDisable(false);
                        timeLabel.setText("");
                        playerProgressName.setText("");
                    });
                    removeGameRoomFromGameRoomList(currentGameRoom);
                    currentGameRoom = new GameRoom();
                    roomId = "";
                    goGame = null;

                    timer.cancel();
                    break;
                case "ban":
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "You were banned on this server!", ButtonType.OK);
                        alert.showAndWait();
                        System.exit(0);
                        Platform.exit();
                    });
                    break;
                case "newUserInfo":
                    Player newUserInfo = new Player(((Element) user).getElementsByTagName("userName").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("gameCount").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("rating").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("percentWins").item(0).getTextContent());
                    setNewInfoAboutPlayer(newUserInfo);
                    break;
                case "roomFull":
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This room is full", ButtonType.OK);
                        alert.showAndWait();
                    });
                    break;
                default:
                    System.out.println("Default:" + input);
                    break;
            }
        } catch (SAXException | IOException e) {
            LOGGER.error("Exception", e);
        }
    }


    /**
     * Changing interval value for timer
     */
    private int setInterval() {
        if (interval == 1) {
            timer.cancel();
            if (currentPlayer.getUserName().equals(playerProgressName.getText())) {
                Platform.runLater(this::playerPassed);
            }
        }
        return --interval;
    }

    /**
     * Start timer when users start game
     * and write mm:ss in timeLabel
     * for one user steps
     */
    private void startTimer() {
        timer.cancel();
        timer = new Timer();
        interval = 180;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Platform.runLater(() -> timeLabel.setText(FORMAT.format(new Date(setInterval() * 1000))));
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        }, 1000, 1000);
    }

    /**
     * Testing radio button id and selected when end search
     *
     * @param id for radio button
     */
    private void setRadioButtonSelected(String id) {
        if (fieldSize5.getId().equals(id)) {
            fieldSize5.selectedProperty().set(true);
        } else if (fieldSize6.getId().equals(id)) {
            fieldSize6.selectedProperty().set(true);
        } else if (fieldSize7.getId().equals(id)) {
            fieldSize7.selectedProperty().set(true);
        } else if (fieldSize8.getId().equals(id)) {
            fieldSize8.selectedProperty().set(true);
        }
    }

    /**
     * Showing game tab with private room when player
     * creates or connecting to room
     * and set values on label name - player name,
     * and default value on label status
     *
     * @param labelName   label name for player name
     * @param labelStatus label status for default value
     * @param playerName  the name for labelName
     */
    private void addPrivateGameTab(Label labelName, Label labelStatus, String playerName) {
        Platform.runLater(() -> {
            tabPane.getTabs().add(privateRoomTab);
            tabPane.setSelectionModel(gameRoomTabSelectionModel);
            labelName.setText(playerName);
            labelStatus.setText("not ready");
        });
    }

    /**
     * Finding and remove player from player list
     *
     * @param player for removing
     */
    private void removePlayerFromPlayersList(Player player) {
        for (Player temp : userObsList) {
            if (temp.getUserName().equals(player.getUserName())) {
                userObsList.remove(userObsList.indexOf(temp));
                break;
            }
        }
    }

    /**
     * Finding and remove game room from game room list
     *
     * @param gameRoom for removing
     */
    private void removeGameRoomFromGameRoomList(GameRoom gameRoom) {
        for (GameRoom temp : gameRoomObsList) {
            if (temp.getHost().equals(gameRoom.getHost())) {
                gameRoomObsList.remove(gameRoomObsList.indexOf(temp));
                break;
            }
        }
    }

    /**
     * Reading xml and return new player object
     *
     * @param element for removing
     * @return new player object
     */
    private Player getPlayerFromXML(Element element) {
        return new Player(element.getElementsByTagName("userName").item(0).getTextContent(),
                element.getElementsByTagName("userGameCount").item(0).getTextContent(),
                element.getElementsByTagName("userRating").item(0).getTextContent(),
                element.getElementsByTagName("userPercentWins").item(0).getTextContent());
    }

    /**
     * Reading xml and return new game room object
     *
     * @param element for removing
     * @return new game room object
     */
    private GameRoom getGameRoomFromXML(Element element) {
        GameRoom gameRoom = new GameRoom(element.getElementsByTagName("roomHost").item(0).getTextContent(),
                element.getElementsByTagName("roomDescription").item(0).getTextContent(),
                element.getElementsByTagName("roomId").item(0).getTextContent());
        gameRoom.setOnline(element.getElementsByTagName("roomOnline").item(0).getTextContent());
        gameRoom.setStatusGame(element.getElementsByTagName("gameStatus").item(0).getTextContent());
        return gameRoom;
    }

    /**
     * Checking what player existing in player list
     *
     * @param player for checking
     * @return true if player exist or else if not
     */
    private boolean checkContainsPlayer(Player player) {
        for (Player temp : userObsList) {
            if (temp.getUserName().equals(player.getUserName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checking what game room existing in game room list
     *
     * @param gameRoom for checking
     * @return true if game room exist or else if not
     */
    private boolean checkContainsGameRoom(GameRoom gameRoom) {
        for (GameRoom temp : gameRoomObsList) {
            if (temp.getHost().equals(gameRoom.getHost())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creating and show new form
     * for creating private game room
     */
    public void createRoom() {
        try {
            Stage createRoomStage = new Stage();
            FXMLLoader fxmlLoaderRoom = new FXMLLoader(getClass().getResource("/createRoom.fxml"));
            Parent createLobby = fxmlLoaderRoom.load();
            createRoomStage.setTitle("Create room");
            createRoomStage.setScene(new Scene(createLobby));
            createRoomStage.setResizable(false);
            createRoomStage.initOwner(Main.mainStage);
            createRoomStage.initModality(Modality.APPLICATION_MODAL);
            CreateRoomController.setClientHandler(clientHandler);
            CreateRoomController.setCurrentStage(createRoomStage);
            CreateRoomController.setPlayerWindowController(this);
            createRoomStage.show();
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Creating xml with info for server that player
     * want close private game room and sends this xml in
     * string format
     */
    public void closeCurrentRoom() {
        Document doc = TransformerAndDocumentFactory.newDocument();
        Element root;
        if (currentGameRoom.getHost().equals(currentPlayer.getUserName())) {
            root = createXML(doc, "closeRoom");
        } else {
            root = createXML(doc, "disconnectingFromRoom");
        }

        Element roomIdElement = doc.createElement("roomId");
        roomIdElement.appendChild(doc.createTextNode(roomId));
        root.appendChild(roomIdElement);

        StringWriter writer = TransformerAndDocumentFactory.transform(doc);

        clientHandler.send(writer.toString());

        connectToRoom.disableProperty().setValue(false);
        roomId = "";
        currentGameRoom = new GameRoom();
        goGame = null;
        Platform.runLater(() -> {
            tabPane.getTabs().remove(privateRoomTab);
            createRoomButton.disableProperty().setValue(false);
            gamePane.getChildren().clear();
            labelPlayerNickName.setText("");
            labelPlayerStatus.setText("");
            buttonReady.disableProperty().setValue(false);
            startGame.disableProperty().setValue(false);
            fieldSizePane.setDisable(false);
            fieldSize5.setSelected(true);
        });
    }

    /**
     * Creating xml with info for server
     * what player change status in game room
     * and sends this xml in string format
     */
    public void changeStatus() {
        String playerType = "not host";
        String currStatus = "";
        if (currentGameRoom.getHost().equals(currentPlayer.getUserName())) {
            if (!labelHostStatus.getText().isEmpty()) {
                currStatus = labelHostStatus.getText();
            }
            playerType = "host";
        } else if (!labelPlayerStatus.getText().isEmpty()) {
            currStatus = labelPlayerStatus.getText();
        }
        if (!currStatus.isEmpty()) {
            switch (currStatus) {
                case "ready":
                    currStatus = "not ready";
                    break;
                case "not ready":
                    currStatus = "ready";
                    break;
                default:
                    currStatus = "unknown status";
                    break;
            }
            Document doc = TransformerAndDocumentFactory.newDocument();

            Element root = createXML(doc, "changeStatus");

            Element idRoomElement = doc.createElement("idRoom");
            idRoomElement.appendChild(doc.createTextNode(roomId));
            root.appendChild(idRoomElement);

            Element statusElement = doc.createElement("status");
            statusElement.appendChild(doc.createTextNode(currStatus));
            root.appendChild(statusElement);

            Element playerTypeElement = doc.createElement("playerType");
            playerTypeElement.appendChild(doc.createTextNode(playerType));
            root.appendChild(playerTypeElement);

            StringWriter writer = TransformerAndDocumentFactory.transform(doc);

            clientHandler.send(writer.toString());
        }

    }

    /**
     * Creating xml with info for server
     * that the player wants to connect to the game room
     * and sends this xml in string format
     */
    public void connectToGameRoom() {
        GameRoom gameRoom = lobbyListTable.getSelectionModel().getSelectedItem();
        if (gameRoom != null) {
            Document doc = TransformerAndDocumentFactory.newDocument();
            Element root = createXML(doc, "connectToRoom");

            Element idRoomElement = doc.createElement("idRoom");
            idRoomElement.appendChild(doc.createTextNode(gameRoom.getIdRoom()));
            root.appendChild(idRoomElement);

            StringWriter writer = TransformerAndDocumentFactory.transform(doc);

            clientHandler.send(writer.toString());
        }
    }

    /**
     * Set new info about current online in room list
     *
     * @param online new online values
     * @param roomId id room for change value
     */
    private void setOnlineInGameRoom(String online, String roomId) {
        ObservableList<GameRoom> newGameRoomObsList = FXCollections.observableArrayList();
        for (GameRoom temp : gameRoomObsList) {
            if (temp.getIdRoom().equals(roomId)) {
                temp.setOnline(online);
            }
            newGameRoomObsList.add(temp);
        }
        gameRoomObsList.clear();
        gameRoomObsList.addAll(newGameRoomObsList);
    }

    /**
     * Set new info about player in player list
     *
     * @param player new player value
     */
    private void setNewInfoAboutPlayer(Player player) {
        ObservableList<Player> newPlayerObsList = FXCollections.observableArrayList();
        for (Player temp : userObsList) {
            if (temp.getUserName().equals(player.getUserName())) {
                temp.setUserGameCount(player.getUserGameCount());
                temp.setUserRating(player.getUserRating());
                temp.setUserPercentWins(player.getUserPercentWins());
            }
            newPlayerObsList.add(temp);
        }
        userObsList.clear();
        userObsList.addAll(newPlayerObsList);
    }

    /**
     * Set new info about game room status
     * in game room list
     *
     * @param status new status values
     * @param roomId id room for change value
     */
    private void setStatusInGameRoom(String status, String roomId) {
        ObservableList<GameRoom> newGameRoomObsList = FXCollections.observableArrayList();
        for (GameRoom temp : gameRoomObsList) {
            if (temp.getIdRoom().equals(roomId)) {
                temp.setStatusGame(status);
            }
            newGameRoomObsList.add(temp);
        }
        gameRoomObsList.clear();
        gameRoomObsList.addAll(newGameRoomObsList);
    }

    /**
     * Starts game when two player ready and
     * sends to server info about this
     */
    public void startGameClick() {
        if (currentGameRoom.getOnline().equals("2/2") && currentGameRoom.getStatusHost().equals("ready")
                && currentGameRoom.getStatusPlayer().equals("ready")) {
            clientHandler.send(startGame());
            currentGameRoom.setStatusGame("in game");
        }
    }

    /**
     * Create xml about players start game
     *
     * @return xml for server in string format
     */
    private String startGame() {
        int numberOfTiles = Integer.parseInt(filedSizeGroup.getSelectedToggle().getUserData().toString());

        Document document = TransformerAndDocumentFactory.newDocument();

        Element root = createXML(document, "startGame");

        Element numberOfTilesElement = document.createElement("numberOfTiles");
        numberOfTilesElement.appendChild(document.createTextNode(Integer.toString(numberOfTiles)));
        root.appendChild(numberOfTilesElement);

        startGame.disableProperty().set(true);

        StringWriter stringWriter = TransformerAndDocumentFactory.transform(document);

        return stringWriter.toString();
    }

    /**
     * Create xml with x,y coordinate and color.
     * Return this xml in string format after player made a move
     *
     * @param x     coordinate
     * @param y     coordinate
     * @param color black or white
     * @return xml for server in string format
     */
    public String sendCoordinatesToServer(double x, double y, String color) {
        Document document = TransformerAndDocumentFactory.newDocument();

        Element root = createXML(document, "playerMove");

        Element xCoordinate = document.createElement("xCoordinate");
        xCoordinate.appendChild(document.createTextNode(Double.toString(x)));
        root.appendChild(xCoordinate);

        Element yCoordinate = document.createElement("yCoordinate");
        yCoordinate.appendChild(document.createTextNode(Double.toString(y)));
        root.appendChild(yCoordinate);

        Element playerColor = document.createElement("playerColor");
        playerColor.appendChild(document.createTextNode(color));
        root.appendChild(playerColor);

        Element userName = document.createElement("userName");
        userName.appendChild(document.createTextNode(currentPlayer.getUserName()));
        root.appendChild(userName);


        StringWriter stringWriter = TransformerAndDocumentFactory.transform(document);

        return stringWriter.toString();
    }

    /**
     * Return the color of the current player
     *
     * @return black if player are  host, and white for simple player
     */
    public String getColorCurrentPlayer() {
        String color;
        if (currentGameRoom.getHost().equals(currentPlayer.getUserName())) {
            color = currentGameRoom.getHostColor();
        } else {
            color = currentGameRoom.getPlayerColor();
        }
        return color;
    }

    /**
     * Create xml about player passed
     * and sends to server name this player
     * in string format
     */
    public void playerPassed() {
        startTimer();
        gamePane.setDisable(true);
        passButton.setDisable(true);

        String name = currentPlayer.getUserName();
        if (name.equals(currentGameRoom.getHost())) {
            Platform.runLater(() -> playerProgressName.setText(currentGameRoom.getPlayer()));
        } else {
            Platform.runLater(() -> playerProgressName.setText(currentGameRoom.getHost()));
        }

        Document document = TransformerAndDocumentFactory.newDocument();

        Element root = createXML(document, "playerPassed");

        Element userName = document.createElement("userName");
        userName.appendChild(document.createTextNode(currentPlayer.getUserName()));
        root.appendChild(userName);


        StringWriter stringWriter = TransformerAndDocumentFactory.transform(document);

        clientHandler.send(stringWriter.toString());
    }

    /**
     * Get radio button id when player click on radio button
     * and sends this info in xml to server
     */
    public void changeFieldSize(MouseEvent mouseEvent) {
        RadioButton radioButton = (RadioButton) mouseEvent.getSource();
        Document document = TransformerAndDocumentFactory.newDocument();

        Element root = createXML(document, "changeFieldSize");

        Element radioButtonId = document.createElement("buttonId");
        radioButtonId.appendChild(document.createTextNode(radioButton.getId()));
        root.appendChild(radioButtonId);

        Element fieldSize = document.createElement("fieldSize");
        fieldSize.appendChild(document.createTextNode(radioButton.getUserData().toString()));
        root.appendChild(fieldSize);

        StringWriter stringWriter = TransformerAndDocumentFactory.transform(document);

        clientHandler.send(stringWriter.toString());
    }

    /**
     * Get user name from user list,
     * creates xml and sends this info to server
     * what this player need banned
     */
    public void banSelectedUser() {
        Player player = userListTable.getSelectionModel().getSelectedItem();
        if (player != null) {
            Document document = TransformerAndDocumentFactory.newDocument();
            Element root = createXML(document, "banUser");

            Element userName = document.createElement("userName");
            userName.appendChild(document.createTextNode(player.getUserName()));
            root.appendChild(userName);

            StringWriter writer = TransformerAndDocumentFactory.transform(document);

            clientHandler.send(writer.toString());
        }
    }

    /**
     * Create root xml element
     * and meta-info element with values
     *
     * @param document for creating element
     * @param metaInfo values for meta-info element
     * @return root element
     */
    public static Element createXML(Document document, String metaInfo) {
        Element root = document.createElement("body");
        document.appendChild(root);

        Element meta = document.createElement("meta-info");
        meta.appendChild(document.createTextNode(metaInfo));
        root.appendChild(meta);

        return root;
    }

    /**
     * Sends to server info about that room need
     * set values in null
     */
    private void setNullCurrentRoom() {
        Document document = TransformerAndDocumentFactory.newDocument();
        createXML(document, "nullCurrentRoom");
        clientHandler.send(TransformerAndDocumentFactory.transform(document).toString());
    }
}
