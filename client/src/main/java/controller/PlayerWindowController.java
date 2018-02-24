package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.ClientHandler;
import view.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;
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
import java.io.IOException;
import java.io.StringReader;


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
    private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder docBuilder;
    private Stage loginStage = new Stage();
    private LoginController loginController;
    private ObservableList<Player> items = FXCollections.observableArrayList();
    private Player currentPlayer;

    @FXML
    public void initialize() throws IOException, ParserConfigurationException {
        docBuilder = docFactory.newDocumentBuilder();
        ClientHandler clientHandler = new ClientHandler();
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
        LoginController.setClientHandler(clientHandler);
    }

    public void getMeta(String input) {
        try {
            Document document = docBuilder.parse(new InputSource(new StringReader(input)));
            Node user = document.getElementsByTagName("body").item(0);
            String meta = ((Element) user).getElementsByTagName("meta-info").item(0).getTextContent();
            meta = meta.substring(0, meta.indexOf(";"));
            Player player;
            switch (meta) {
                case "connect":
                    Platform.runLater(() -> {
                        Main.mainStage.show();
                        loginStage.close();
                    });
                    currentPlayer = getPlayer(((Element) user).getElementsByTagName("meta-info").item(0).getTextContent());
                    items.add(currentPlayer);
                    userListTable.setItems(items);
                    helloUser.setText("Hello " + currentPlayer.getUserName());
                    break;
                case "incorrect":
                    Platform.runLater(() -> loginController.setErrorLabel("Login or password are incorrect!!!"));
                    break;
                case "newUserconnect":
                    player = getPlayer(((Element) user).getElementsByTagName("meta-info").item(0).getTextContent());
                    if (!player.getUserName().equals(currentPlayer.getUserName())){
                        items.add(player);
                    }
                    break;
                default:
                    System.out.println("Default:" + input);
                    break;
            }
        } catch (SAXException | IOException e) {
            logger.error("Exception", e);
        }
    }

    public Player getPlayer(String metaInfo) {
        metaInfo = metaInfo.substring(metaInfo.indexOf(";") + 1);
        String userName = truncateMetaInfo(metaInfo);
        metaInfo = metaInfo.substring(metaInfo.indexOf(";") + 1);
        String userGameCount = truncateMetaInfo(metaInfo);
        metaInfo = metaInfo.substring(metaInfo.indexOf(";") + 1);
        String userPercentWins = truncateMetaInfo(metaInfo);
        metaInfo = metaInfo.substring(metaInfo.indexOf(";") + 1);
        String userRating = truncateMetaInfo(metaInfo);
        return new Player(userName, userGameCount, userRating, userPercentWins);
    }

    public String truncateMetaInfo(String string) {
        return string.substring(0, string.indexOf(";"));
    }
}
