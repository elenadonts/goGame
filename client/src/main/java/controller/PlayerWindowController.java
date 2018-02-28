package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.ClientHandler;
import model.GameRoom;
import sun.awt.geom.AreaOp;
import view.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Player;
import org.apache.log4j.Logger;
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


public class PlayerWindowController {
    private static final Logger logger = Logger.getLogger(PlayerWindowController.class);
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
    private ClientHandler clientHandler = new ClientHandler();

    private SingleSelectionModel<Tab> gameRoomTabSelectionModel;
    private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder docBuilder;
    private Stage loginStage = new Stage();
    private LoginController loginController;
    private ObservableList<Player> userObsList = FXCollections.observableArrayList();
    private ObservableList<GameRoom> gameRoomObsList = FXCollections.observableArrayList();
    private Player currentPlayer;
    private GameRoom currentGameRoom = new GameRoom();
    private String roomId;

    @FXML
    public void initialize() throws IOException, ParserConfigurationException {
        docBuilder = docFactory.newDocumentBuilder();
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
    }

    public void readXML(String input) {
        try {
            System.out.println("ПРИНИМАЕМ " + input);
            Document document = docBuilder.parse(new InputSource(new StringReader(input)));
            Node user = document.getElementsByTagName("body").item(0);
            String metaInfo = ((Element) user).getElementsByTagName("meta-info").item(0).getTextContent();
            if (metaInfo.contains(";")) {
                metaInfo = metaInfo.substring(0, metaInfo.indexOf(";"));
            }
            Player player;
            GameRoom gameRoom;
            switch (metaInfo) {
                case "connect":
                    Platform.runLater(() -> {
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
                    Platform.runLater(() -> {
                        labelHostNickName.setText(currentGameRoom.getHost());
                        labelHostStatus.setText(((Element) user).getElementsByTagName("hostStatus").item(0).getTextContent());
                    });

                    createRoomButton.disableProperty().setValue(true);
                    connectToRoom.disableProperty().setValue(true);
                    startGame.disableProperty().setValue(true);
                    break;

                case "playerConnectToRoom":
                    Platform.runLater(() -> {
                        labelPlayerNickName.setText(((Element) user).getElementsByTagName("playerName").item(0).getTextContent());
                        labelPlayerStatus.setText("not-ready");
                    });
                    break;
                case "changeOnline":
                    setOnlineInGameRoom(((Element) user).getElementsByTagName("playerOnline").item(0).getTextContent(),
                            ((Element) user).getElementsByTagName("roomId").item(0).getTextContent());
                    break;

                case "playerDisconnect":
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
                    });
                    createRoomButton.disableProperty().setValue(false);
                    break;
                default:
                    System.out.println("Default:" + input);
                    break;
            }
        } catch (SAXException | IOException e) {
            logger.error("Exception", e);
        }
    }


    private void addPrivateGameTab(Label labelName, Label labelStatus, String playerName) {
        Platform.runLater(() -> {
            tabPane.getTabs().add(privateRoomTab);
            tabPane.setSelectionModel(gameRoomTabSelectionModel);
            labelName.setText(playerName);
            labelStatus.setText("not ready");
        });
    }

    private void removePlayerFromPlayersList(Player player) {
        for (Player temp : userObsList) {
            if (temp.getUserName().equals(player.getUserName())) {
                userObsList.remove(userObsList.indexOf(temp));
                break;
            }
        }
    }

    private void removeGameRoomFromGameRoomList(GameRoom gameRoom) {
        for (GameRoom temp : gameRoomObsList) {
            if (temp.getHost().equals(gameRoom.getHost())) {
                gameRoomObsList.remove(gameRoomObsList.indexOf(temp));
                break;
            }
        }
    }

    private Player getPlayerFromXML(Element element) {
        return new Player(element.getElementsByTagName("userName").item(0).getTextContent(),
                element.getElementsByTagName("userGameCount").item(0).getTextContent(),
                element.getElementsByTagName("userRating").item(0).getTextContent(),
                element.getElementsByTagName("userPercentWins").item(0).getTextContent());
    }

    private GameRoom getGameRoomFromXML(Element element) {
        return new GameRoom(element.getElementsByTagName("roomHost").item(0).getTextContent(),
                element.getElementsByTagName("roomDescription").item(0).getTextContent(),
                element.getElementsByTagName("roomId").item(0).getTextContent());
    }

    private boolean checkContainsPlayer(Player player) {
        for (Player temp : userObsList) {
            if (temp.getUserName().equals(player.getUserName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkContainsGameRoom(GameRoom gameRoom) {
        for (GameRoom temp : gameRoomObsList) {
            if (temp.getHost().equals(gameRoom.getHost())) {
                return true;
            }
        }
        return false;
    }

    public void createRoom(MouseEvent mouseEvent) {
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
            CreateRoomController.setCurrentPlayer(currentPlayer);
            createRoomStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeCurrentRoom(MouseEvent mouseEvent) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter writer = new StringWriter();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("body");
        doc.appendChild(root);

        Element meta = doc.createElement("meta-info");
        root.appendChild(meta);

        tabPane.getTabs().remove(privateRoomTab);
        createRoomButton.disableProperty().setValue(false);

        if (currentGameRoom.getHost().equals(currentPlayer.getUserName())) {
            meta.appendChild(doc.createTextNode("closeRoom"));
        } else {
            meta.appendChild(doc.createTextNode("disconnectingFromRoom"));
        }
        Element roomIdElement = doc.createElement("roomId");
        roomIdElement.appendChild(doc.createTextNode(roomId));
        root.appendChild(roomIdElement);
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        clientHandler.send(writer.toString());
        connectToRoom.disableProperty().setValue(false);
        roomId = "";
        currentGameRoom = new GameRoom();
    }

    public void changeStatus(MouseEvent mouseEvent) throws TransformerException {
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
            }
            Document doc = docBuilder.newDocument();

            Element root = doc.createElement("body");
            doc.appendChild(root);

            Element meta = doc.createElement("meta-info");
            meta.appendChild(doc.createTextNode("changeStatus"));
            root.appendChild(meta);

            Element idRoomElement = doc.createElement("idRoom");
            idRoomElement.appendChild(doc.createTextNode(roomId));
            root.appendChild(idRoomElement);

            Element statusElement = doc.createElement("status");
            statusElement.appendChild(doc.createTextNode(currStatus));
            root.appendChild(statusElement);

            Element playerTypeElement = doc.createElement("playerType");
            playerTypeElement.appendChild(doc.createTextNode(playerType));
            root.appendChild(playerTypeElement);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            clientHandler.send(writer.toString());
        }

    }

    public void connectToGameRoom(MouseEvent mouseEvent) throws TransformerException {
        GameRoom gameRoom = lobbyListTable.getSelectionModel().getSelectedItem();
        if (gameRoom != null) {
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("body");
            doc.appendChild(root);

            Element meta = doc.createElement("meta-info");
            meta.appendChild(doc.createTextNode("connectToRoom"));
            root.appendChild(meta);

            Element idRoomElement = doc.createElement("idRoom");
            idRoomElement.appendChild(doc.createTextNode(gameRoom.getIdRoom()));
            root.appendChild(idRoomElement);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            clientHandler.send(writer.toString());
        }
    }

    public void setOnlineInGameRoom(String online, String roomId) {
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

    public void startGameClick(MouseEvent mouseEvent) {
        if (currentGameRoom.getStatusHost().equals("ready") && currentGameRoom.getStatusPlayer().equals("ready")){
            System.out.println("game start!go dance)");
        }
    }
}
